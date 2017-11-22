import tensorflow as tf
from os.path import join

# Specify input flags
flags = tf.flags
flags.DEFINE_string("data_path", None, "Path of the folder containing the .tfrecord files")
flags.DEFINE_string("save_path", None, "Path to the folder in which the trained models should be saved")
FLAGS = flags.FLAGS

print(FLAGS)