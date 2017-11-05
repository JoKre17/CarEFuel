import math
import csv
import random
import progressbar

import tensorflow as tf
import numpy as np

from os import listdir
from os.path import isfile, join
from datetime import datetime
from scipy import interpolate

'''
    This script converts the available gas price data into a format that's favourable for training. It creates the three
    files training.tfrecords validation.tfrecords and test.tfrecord, in which all available data is divided into
    training, validation and test data.
'''

# Parse user input
flags = tf.flags
flags.DEFINE_string("data_path", None, "Path of the folder containing prices of gas stations")
FLAGS = flags.FLAGS

# Ratios in which to split the data (training, validation, test)
dataset_ratios = (0.8, 0.1, 0.1)

# Number of data samples taken per file
n_samples_per_file = 5

# Extract distinct months (last month and previous months) for training of the neural network
hours_per_month = 31 * 24


def _create_tfrecord(name, files):
    print("Creating ", name)
    # Create a new tfrecord file 'name' from files
    writer = tf.python_io.TFRecordWriter(name)

    # Show progress of the loop
    with progressbar.ProgressBar(max_value=len(files)) as bar:
        # Iterate over files
        for index, file in enumerate(files):
            # Open file and parse input
            entries = []
            with open(file, "r") as csvfile:
                reader = csv.reader(csvfile, delimiter=';')
                for row in reader:
                    entries.append(row)

            # Abort if file empty
            if len(entries) < 2:
                print("Aborted, ", file, " contains not enough entries")
                continue

            # Parse entries to a new format: The first entry is seen has hour zero. All following entries are relative
            # to the first one.
            hours = []
            prices = []
            reference_date = _parse_str_date(entries[0][0])

            # Iterate over all entries
            for entry in entries:
                time_diff = _parse_str_date(entry[0]) - reference_date
                hours.append(time_diff.total_seconds() / 3600.0)
                prices.append(int(entry[1]))

            # Interpolate the data at every hour
            f = interpolate.interp1d(hours, prices, kind='linear')
            hours = np.linspace(0, hours[-1], hours[-1] + 1)
            prices = f(hours)

            # On top of just the last month of the data, generate n_samples_per_file random hours in the second half of
            # the dataset that are each the last known prices for another new data set
            current_index = len(prices) - hours_per_month
            for _ in range(n_samples_per_file):
                # Generate data set
                example = _create_data_set(prices, current_index)

                if example is None:
                    break

                # Serialize to string and write to the file
                writer.write(example.SerializeToString())

                # Generate random new index in the second half of the data set
                current_index = random.randint(math.floor(len(prices) / 2), len(prices) - hours_per_month)

                # Abort if not enough previous months
                if current_index - hours_per_month < 0:
                    print("Aborted, not enough entries for current index")
                    break

            # Update progress bar
            bar.update(index)


    # Cleanup
    writer.close()


def _create_data_set(_prices, month_index):
    next_month = _prices[month_index:month_index + hours_per_month]
    prev_months = []

    n_prev_months = 0
    while (month_index - hours_per_month) >= 0:
        month_index -= hours_per_month
        prev_months.append(_prices[month_index:month_index + hours_per_month])

        n_prev_months += 1

    # Abort if not enough months available
    if len(prev_months) == 0:
        return None

    # Create feature
    feature = {'prev_months': tf.train.Feature(float_list=
                                               tf.train.FloatList(value=np.array(prev_months).reshape(-1))),
               'next_month': tf.train.Feature(float_list=
                                              tf.train.FloatList(value=np.array(next_month))),
               'n_prev_months': tf.train.Feature(int64_list=
                                                 tf.train.Int64List(value=[n_prev_months]))}

    # Create an example protocol buffer
    example = tf.train.Example(features=tf.train.Features(feature=feature))

    return example


def _parse_str_date(date):
    date += "00"
    return datetime.strptime(date, "%Y-%m-%d %H:%M:%S%z")


def main(_):
    if not FLAGS.data_path:
        raise ValueError("You need to specify data_path!")

    # Get all input file names sorted by name
    files = [f for f in listdir(FLAGS.data_path) if isfile(join(FLAGS.data_path, f))]
    files.sort()

    # Add full path to files
    files = [join(FLAGS.data_path, file) for file in files]

    # Split all data into three parts (training, validation, testing) depending on the ratios defined in config
    training_index = 0
    validation_index = math.floor(dataset_ratios[0] * len(files))
    testing_index = math.floor(len(files) - dataset_ratios[2] * len(files))

    # Create a new tfrecords file for each data set
    _create_tfrecord("training.tfrecord", files[training_index:validation_index - 1])
    _create_tfrecord("validation.tfrecord", files[validation_index:testing_index - 1])
    _create_tfrecord("testing.tfrecord", files[testing_index:])

if __name__ == '__main__':
    tf.app.run()

