import tensorflow as tf
from os.path import join

# Specify input flags
flags = tf.flags
flags.DEFINE_string("data_path", None, "Path of the folder containing the .tfrecord files")
flags.DEFINE_string("save_path", None, "Path to the folder in which the trained models should be saved")
FLAGS = flags.FLAGS


class Config:
    """
    Config class: Hyperparameters for this training and the model

    dataset_ratios: Split all available data into training, validation and testing data according these
    n_hours_month: Number of hours per month (assuming every month has 31 days). Also defines the number of unrolls of
        of the network due to the LSTM cells
    batch_size: Training parameter: Batch size for every training step
    num_epochs: Specifies how often the network is trained on the complete dataset
    LSTM_layer: Number of hidden LSTM layers of the network
    max_price: Maximum gas price in ct * 10. Used to scale all prices to [0,1] -> better for network
    """
    dataset_ratios = (0.8, 0.1, 0.1)
    n_hours_per_month = 744
    max_past_months = 50
    batch_size = 5
    num_epochs = 50
    hidden_layer = 5
    max_price = 3000


def input_fn(path):
    """
    Define the input function for the tf.Estimator during training. Needs to be wrapped, because tf.Estimator needs a
    function object without arguments.

    It uses one of the three files "training.tfrecord", "validation.tfrecord" or "testing.tfrecord", specified by path,
    and uses the tf.Dataset class for handling the input. It returns a batch of tensors with price data from previous
    months and corresponding prices of the next month.
    """

    # In general perform all input computing on the CPU
    with tf.device('/cpu:0'):
        # Open TFRecord file
        dataset = tf.data.TFRecordDataset(path)

        # Convert into tensors
        def _parse_tfrecord_data(feature):
            keys = {'prev_months': tf.VarLenFeature(tf.float32),
                    'next_month': tf.VarLenFeature(tf.float32),
                    'n_prev_months': tf.FixedLenFeature([], tf.int64)}

            feature = tf.parse_single_example(feature, features=keys)
            n_prev_months = tf.cast(feature['n_prev_months'], tf.int32)
            _next_month = tf.sparse_tensor_to_dense(feature['next_month'], default_value=0)
            _prev_months = tf.sparse_tensor_to_dense(feature['prev_months'], default_value=0)
            _prev_months = tf.reshape(_prev_months, [-1, Config.n_hours_per_month])

            # Need to append empty tensors to _prev_months, otherwise dataset.batch won't work. All tensors need to be equal
            n_missing_entries = Config.max_past_months - n_prev_months
            zero_tensors = tf.zeros([n_missing_entries, Config.n_hours_per_month])
            _prev_months = tf.concat([_prev_months, zero_tensors], 0)

            return _prev_months, _next_month

        # Apply transformations
        dataset = dataset.map(_parse_tfrecord_data)
        dataset = dataset.batch(Config.batch_size)
        dataset = dataset.repeat(Config.num_epochs)
        iterator = dataset.make_one_shot_iterator()
        prev_months, next_month = iterator.get_next()

        return prev_months, next_month


def model_fn():
    # Input layer

def main(_):
    if not FLAGS.data_path:
        raise ValueError("You need to specify data_path!")

    path = join(FLAGS.data_path, "test.tfrecord")
    input = input_fn(path)

    sess = tf.Session()
    print(sess.run(input))

if __name__ == '__main__':
    tf.app.run()

