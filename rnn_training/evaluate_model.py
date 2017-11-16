import tensorflow as tf
import rnn_training.train as train
import matplotlib.pyplot as plt


# Specify input flags
flags = tf.flags
flags.DEFINE_string("model_path", None, "Path of the folder containing the saved model")
FLAGS = flags.FLAGS


def evaluate(next_month, prediction):
    """
    This function evaluates the quality of the next months' prediction with regard to
    the actual next month
    """
    plt.figure(1)

    plt.plot(next_month[0:48], label='next month')
    plt.plot(prediction[0:48], label='prediction')
    plt.show()


def main(_):
    # Restore the model
    with tf.Session(graph=tf.Graph()) as sess:
        # Load the the saved model
        tf.saved_model.loader.load(sess, ['serve'], FLAGS.model_path)

        # Initialize variables
        tf.global_variables_initializer()

        # Load the input files
        features, next_month = train.input_fn("testing.tfrecord")
        prev_months = features['prev_months']
        n_prev_months = features['n_prev_months']
        prev_months = sess.run(prev_months)
        n_prev_months = sess.run(n_prev_months)
        next_month = sess.run(next_month)
        next_month = next_month[0]

        # Fetch the output tensor
        output_tensor = sess.graph.get_tensor_by_name("Output/rescaled_output:0")

        # Run the network
        result = sess.run(fetches=output_tensor, feed_dict={"Input/prev_months:0": prev_months,
                                                            "Input/n_prev_months:0": n_prev_months})
        result = result[0]

        evaluate(next_month, result)

if __name__ == '__main__':
    tf.app.run()
