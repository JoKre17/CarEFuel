import tensorflow as tf
import numpy as np

import os

# Specify input flags
flags = tf.flags
flags.DEFINE_string("data_path", 'data', "Path of the folder containing the .tfrecord files")
flags.DEFINE_string("save_path", 'model', "Path to the folder in which the trained models should be saved")
flags.DEFINE_string("batch_size", None, "Batch Size to use during training")
flags.DEFINE_string("num_epochs", None, "Number of training steps on the whole data set")
flags.DEFINE_string("keep_prob", None, "Dropout probability for all layers during training")
flags.DEFINE_string("learning_rate", None, "Learning Rate for the Adam Optimizer")

FLAGS = flags.FLAGS

# Activate verbose logging
tf.logging.set_verbosity(tf.logging.INFO)

"""
    Parameter dict: Hyperparameters for this training and the model

    n_hours_month: Number of hours per month (assuming every month has 31 days). Also defines the number of unrolls of
        of the network due to the LSTM cells. Currently set to 31 * 24 / 2, as all available data is interpolated only
        every two hours, due to the fact that on average all gas stations have 8 - 10 entries per day.
    batch_size: Training parameter: Batch size for every training step
    num_epochs: Specifies how often the network is trained on the complete dataset
    max_price: Maximum gas price in ct * 10. Used to scale all prices to [0,1] -> better for network
    keep_prob: Dropout probability for cells during training
    learning_rate: Learning rate during training. TODO: add learning decay
"""
params = {
    'n_hours_per_month': 372,
    'max_past_months': 50,
    'batch_size': 1,
    'num_epochs': 10,
    'max_price': 3000,  # TODO: better normalization
    'keep_prob': 0.5,
    'learning_rate': 0.1,
    'prefetched_elements': 50
}


def input_fn(path):
    """
    Define the input function for the tf.Estimator during training. Needs to be wrapped, because tf.Estimator needs a
    function object without arguments.

    It uses one of the three files "training.tfrecord", "validation.tfrecord" or "testing.tfrecord", specified by path,
    and uses the tf.Dataset class for handling the input. It returns a batch of tensors with price data from previous
    months and corresponding prices of the next month.
    """

    # In general perform all input computing on the CPU, letting GPU concentrate on training
    with tf.device('/cpu:0'):
        # Open TFRecord file
        dataset = tf.data.TFRecordDataset(path)

        # Prefetch data for more efficient hardware utilization
        dataset = dataset.prefetch(params['prefetched_elements'])

        # Convert into tensors
        def _parse_tfrecord_data(feature):
            keys = {'prev_months': tf.VarLenFeature(tf.float32),
                    'next_month': tf.VarLenFeature(tf.float32),
                    'n_prev_months': tf.FixedLenFeature([], tf.int64)}

            feature = tf.parse_single_example(feature, features=keys)
            n_prev_months = tf.cast(feature['n_prev_months'], tf.int32)
            _next_month = tf.sparse_tensor_to_dense(feature['next_month'], default_value=0)
            _prev_months = tf.sparse_tensor_to_dense(feature['prev_months'], default_value=0)
            _prev_months = tf.reshape(_prev_months, [-1, params['n_hours_per_month']])

            # Need to append empty tensors to _prev_months, otherwise dataset.batch won't work.
            # -> All tensors need to have equal shape
            n_missing_entries = params['max_past_months'] - n_prev_months
            zero_tensors = tf.zeros([n_missing_entries, params['n_hours_per_month']])
            _prev_months = tf.concat([_prev_months, zero_tensors], 0)

            # Make the number of previous months a feature in order be able to dynamically roll out LSTMs during
            # training
            return {'prev_months': _prev_months, 'n_prev_months': n_prev_months}, _next_month

        # Apply transformations
        dataset = dataset.map(_parse_tfrecord_data)
        dataset = dataset.batch(params['batch_size'])
        iterator = dataset.make_one_shot_iterator()
        features, next_month = iterator.get_next()

        return features, next_month


def model_fn(features, labels, mode):
    """
    :param features: Contains a dict with the input data from the previous months in the
        shape (batch_size, max_past_months, n_hours_per_month) and the number of actual previous months in shape
        (batch_size, n_prev_months)
    :param labels: Contains the next month(s) in shape of (batch_size, n_hours_per_month)
    :param mode: Value indicating whether this model is created for training, validation or testing
    :return: The Estimator spec for this model
    """

    # Extract prev_months tensors that also contain the null tensors(see input_fn) and corresponding n_prev_months
    n_prev_months = features['n_prev_months']
    prev_months = features['prev_months']

    # Add names for later usage
    with tf.name_scope("Input"):
        n_prev_months = tf.identity(n_prev_months, name="n_prev_months")
        prev_months = tf.identity(prev_months, name="prev_months")

    # Input layer: scale prices into [0,1] -> network can better handle values of that size
    inputs = prev_months / params['max_price']

    '''Create hidden layer'''
    def lstm_layer():
        """ Function to create LSTM cells and incorporate dropout during training"""
        cell = tf.nn.rnn_cell.LSTMCell(params['n_hours_per_month'], forget_bias=1.0, state_is_tuple=True)
        if mode == tf.estimator.ModeKeys.TRAIN:
            cell = tf.contrib.rnn.DropoutWrapper(cell, output_keep_prob=params['keep_prob'])
        return cell

    def dense_layer(input, size):
        """Function to create a dense layer with 'size' nodes that applies dropout during training"""
        if mode == tf.estimator.ModeKeys.TRAIN and params['keep_prob'] < 1:
            input = tf.nn.dropout(input, params['keep_prob'])

        # Combine output in a densely connected layer
        return tf.layers.dense(inputs=input, units=size, activation=tf.nn.relu)

    # Create two hidden LSTM layers
    layers = [lstm_layer(), lstm_layer()]
    hidden_layers = tf.contrib.rnn.MultiRNNCell(layers, state_is_tuple=True)

    ''' ------------MAGIC! Unrolling of the RNN network-------------'''
    # Input is now a list of length max_past_months of tensors with shape (batch_size, n_hours_per_month)
    input = tf.unstack(inputs, num=params['max_past_months'], axis=1)

    # Providing the sequence_length parameter allows for dynamic calculation. Only n_prev_months are calculated,
    # the last entries of rnn_output contain only zeros!
    rnn_output, _ = tf.contrib.rnn.static_rnn(hidden_layers, input, sequence_length=n_prev_months,
                                              dtype=tf.float32)

    '''
    Hack to build the indexing and retrieve the right output 
    (from https://github.com/aymericdamien/TensorFlow-Examples/blob/master/examples/3_NeuralNetworks/dynamic_rnn.py
    '''
    # 'output' is a list of output at every timestep, we pack them in a Tensor
    # and change back dimension to [batch_size, n_step, n_input]
    rnn_output = tf.stack(rnn_output)
    output = tf.transpose(rnn_output, [1, 0, 2])

    batch_size = tf.shape(output)[0]
    # Start indices for each sample
    index = tf.range(0, batch_size) * params['max_past_months'] + (n_prev_months - 1)
    # Indexing of the last calculated element
    output = tf.gather(tf.reshape(output, [-1, params['n_hours_per_month']]), index)

    ''' One Dense Layer at the end '''
    size = params['n_hours_per_month']
    # Apply dropout if training
    if mode == tf.estimator.ModeKeys.TRAIN and params['keep_prob'] < 1:
        output = tf.nn.dropout(output, params['keep_prob'])

    output = tf.layers.dense(inputs=output, units=size, activation=tf.nn.relu)

    # Rescale and give name for later usage
    with tf.name_scope("Output"):
        rescaled_output = output * params['max_price']
        rescaled_output = tf.identity(rescaled_output, name="rescaled_output")

    loss = None
    train_op = None
    if mode != tf.estimator.ModeKeys.PREDICT:
        # Loss function
        next_month = labels / params['max_price']
        loss = tf.losses.absolute_difference(next_month, output)

        # Define optimizer for training
        optimizer = tf.train.AdamOptimizer(params["learning_rate"])
        train_op = optimizer.minimize(loss=loss, global_step=tf.train.get_global_step())

    return tf.estimator.EstimatorSpec(
        mode=mode,
        loss=loss,
        train_op=train_op,
        predictions=rescaled_output if mode == tf.estimator.ModeKeys.PREDICT else None,
        export_outputs={'output': tf.estimator.export.PredictOutput({'output': rescaled_output})})


def save_model(estimator, name):
    """Takes an estimator and stores the meta graph containing the trained variables to FLAGS.save_path"""
    path = os.path.join(FLAGS.save_path, name)
    estimator.export_savedmodel(path, tf.estimator.export.build_raw_serving_input_receiver_fn(
        {
            'prev_months': tf.placeholder(dtype=tf.float32,
                                          shape=[1, params['max_past_months'], params['n_hours_per_month']]),
            'n_prev_months': tf.placeholder(dtype=tf.int32, shape=[1]),
        }))


def train_network(fuel_type):
    """Train a network with all data available of the given fuel type"""

    # Build the estimator based on the model defined above
    nn = tf.estimator.Estimator(model_fn=model_fn, model_dir=os.path.join(FLAGS.save_path, fuel_type))

    training_file_path = os.path.join(FLAGS.data_path, fuel_type + "_training")
    validation_file_path = os.path.join(FLAGS.data_path, fuel_type + "_validation")
    testing_file_path = os.path.join(FLAGS.data_path, fuel_type + "_testing")

    # Run training
    for i in range(params['num_epochs']):
        print("Epoch ", i + 1, ":")
        nn.train(input_fn=lambda: input_fn(training_file_path))

        print("Evaluation:")
        #nn.evaluate(input_fn=lambda: input_fn(validation_file_path))

    print("Testing:")
    #nn.evaluate(input_fn=lambda: input_fn(testing_file_path))

    # Export the trained model
    save_model(nn, fuel_type)


def main(_):
    # Parse input
    if not FLAGS.data_path:
        raise ValueError("You need to specify data_path!")
    if not FLAGS.save_path:
        raise ValueError("You need to specify save_path!")
    if FLAGS.batch_size:
        params["batch_size"] = int(FLAGS.batch_size)
    if FLAGS.keep_prob:
        params["keep_prob"] = float(FLAGS.keep_prob)
    if FLAGS.learning_rate:
        params["learning_rate"] = float(FLAGS.learning_rate)
    if FLAGS.batch_size:
        params["batch_size"] = int(FLAGS.batch_size)

    # Train a network for each fuel type
    train_network('e5')
    train_network('e10')
    train_network('diesel')


if __name__ == '__main__':
    tf.app.run()
