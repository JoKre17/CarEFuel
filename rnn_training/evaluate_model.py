import tensorflow as tf
import train as train
import matplotlib.pyplot as plt


# Specify input flags
flags = tf.flags
flags.DEFINE_string("model_path", "model/e5/trained_model", "Path of the folder containing the saved model")
flags.DEFINE_string("testing_data_path", "data/e5_testing", "Path of the corresponding tf_record testing file")
FLAGS = flags.FLAGS


def evaluate(next_month, prediction):
    """
    This function evaluates the quality of the next months' prediction with regard to
    the actual next month
    """
    next_month *= 0.1
    prediction *= 0.1
    plt.figure(1)

    for i in range(len(next_month)):
        plt.plot(next_month[i][0:24], label='next month')
        plt.plot(prediction[i][0:24], label='prediction')
        plt.legend()
        plt.show()


def main(_):
    # Restore the model
    with tf.Session(graph=tf.Graph()) as sess:
        # Load the the saved model
        tf.saved_model.loader.load(sess, ['serve'], FLAGS.model_path)

        # Load the input files
        features, next_month = train.input_fn(FLAGS.testing_data_path)
        prev_months = features['prev_months']
        n_prev_months = features['n_prev_months']
        prev_months = sess.run(prev_months)
        n_prev_months = sess.run(n_prev_months)
        next_month = sess.run(next_month)

        # Fetch the output tensor
        output_tensor = sess.graph.get_tensor_by_name("Output/rescaled_output:0")

        # Run the network
        result = sess.run(fetches=output_tensor, feed_dict={"Input/prev_months:0": prev_months,
                                                            "Input/n_prev_months:0": n_prev_months})

        evaluate(next_month, result)


if __name__ == '__main__':
    tf.app.run()
