import math
import random
import progressbar
import psycopg2

import tensorflow as tf
import numpy as np

from scipy import interpolate

'''
    This script converts the available gas price data into a format that's favourable for training. It creates the three
    files training.tfrecords validation.tfrecords and test.tfrecord, in which all available data is divided into
    training, validation and test data. When looking at the average entries per day it becomes clear that it should be
    sufficient to interpolate the data every two hours.
'''

# Parse user input
flags = tf.flags
flags.DEFINE_string("data_path", 'data_', "Path of the folder containing prices of gas stations")
FLAGS = flags.FLAGS

# Ratios in which to split the data (training, validation, test)
dataset_ratios = (0.8, 0.1, 0.1)

# Number of data samples taken per file
n_samples_per_file = 5

# Extract distinct months of 'hours_per_month' hours(last month and previous months) for training of the neural network
hours_per_month = 31 * 24

# Number of data points per month
n_data_points = int(hours_per_month / 2)


def append_to_tfrecord(writer, dates, price_data):
    """Append the data to the current writer"""

    # First, clean the data
    dates, price_data = clean_data(dates, price_data)

    # Ignore, if nothing to write
    if len(dates) <= n_data_points:
        return

    # Parse entries to a new format: The first entry is seen as hour zero. All following entries are relative
    # to the first one.
    hours = []
    prices = []
    reference_date = dates[0]

    # Iterate over all entries
    for index, date in enumerate(dates):
        time_diff = date - reference_date
        hours.append(time_diff.total_seconds() / 3600.0)
        prices.append(int(price_data[index]))

    # Interpolate the data at every two hours
    f = interpolate.interp1d(hours, prices, kind='linear')
    hours = np.linspace(0, hours[-1], (hours[-1] + 1) / 2)
    prices = f(hours)

    # On top of just the last month of the data, generate n_samples_per_file random hours in the second half of
    # the dataset that are each the last known prices for another new data set
    current_index = len(prices) - n_data_points
    for _ in range(n_samples_per_file):
        # Generate data set
        example = create_data_set(prices, current_index)

        if example is None:
            break

        # Serialize to string and write to the file
        writer.write(example.SerializeToString())

        # Generate random new index in the second half of the data set
        current_index = random.randint(math.floor(len(prices) / 2), len(prices) - n_data_points)

        # Abort if not enough previous months
        if current_index - n_data_points < 0:
            print("Aborted, not enough entries for current index")
            break


def create_data_set(_prices, month_index):
    """Creates a new data set from all data in _prices. Month index represents the first entry of the month that
    is going to be predicted"""
    next_month = _prices[month_index:month_index + n_data_points]
    prev_months = []

    n_prev_months = 0
    while (month_index - n_data_points) >= 0:
        month_index -= n_data_points
        prev_months.append(_prices[month_index:month_index + n_data_points])

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


def load_data_from_database():
    """This generator function returns the data of each gas station one after another"""

    # Connect to an existing database
    conn_string = "host='localhost' dbname='carefuel' user='postgres' password='NJuJh1A!Wln..'"
    conn = psycopg2.connect(conn_string)

    # Open a cursor to perform database operations
    cur = conn.cursor()

    # Get all gas station IDs
    cur.execute("SELECT ID FROM gas_station;")
    gas_station_IDs = cur.fetchall()

    # Iterate over all gas stations
    with progressbar.ProgressBar(max_value=len(gas_station_IDs)) as bar:
        for index, ID in enumerate(gas_station_IDs):
            bar.update(index)
            try:
                # Get all prices of this database
                cur.execute("SELECT date, e5, e10, diesel FROM gas_station_information_history WHERE stid='" + ID[0] +
                            "' ORDER BY date ASC;")
                results = cur.fetchall()

                # Extract into new data structure
                dates = []
                e5 = []
                e10 = []
                diesel = []

                # Ignore gas stations with not enough entries
                if results is None:
                    continue
                if len(results) < 400:
                    continue

                for entry in results:
                    dates.append(entry[0])
                    e5.append(entry[1])
                    e10.append(entry[2])
                    diesel.append(entry[3])

                yield dates, {'e5': e5, 'e10': e10, 'diesel': diesel}
            except Exception as e:
                print("Error at index", index)
                print(e)
                continue

    # Close communication with the database
    cur.close()
    conn.close()


def clean_data(dates, prices):
    """
    This function takes the two lists of dates and prices and returns two new lists that are cleared of errenous data
    """
    new_dates = []
    new_prices = []
    for i, price in enumerate(prices):
        # Assume that every price that is < 100 and > 3000, is garbage
        if 100 < price < 3000:
            new_dates.append(dates[i])
            new_prices.append(price)

    return new_dates, new_prices


def main(_):
    # Number of gas stations in database
    n_gas_stations = 15110

    # Split all data into three parts (training, validation, testing) depending on the ratios defined in config
    training_index = 0
    validation_index = math.floor(dataset_ratios[0] * n_gas_stations)
    testing_index = math.floor(n_gas_stations - dataset_ratios[2] * n_gas_stations)

    # Create a tf_record writer for each file
    writers = {}
    for data_set in ['training', 'validation', 'testing']:
        for fuel_type in ['e5', 'e10', 'diesel']:
            path = FLAGS.data_path + "/" + fuel_type + '_' + data_set
            writers[fuel_type + '_' + data_set] = tf.python_io.TFRecordWriter(path)

    # Iterate over all gas stations using a generator function, just in case something breaks during execution, handle
    # the exception in a way that the files can still be closed gracefully
    try:
        gas_station_gen_func = load_data_from_database()
        for index, data in enumerate(gas_station_gen_func):
            # Unpack data
            dates = data[0]
            fuel_prices = data[1]

            # Assign correct data set
            if index < validation_index:
                data_set = 'training'
            elif index < testing_index:
                data_set = 'validation'
            else:
                data_set = 'testing'

            # Append new data to file in parallel
            for fuel_type in ['e5', 'e10', 'diesel']:
                append_to_tfrecord(writers[fuel_type + '_' + data_set], dates, fuel_prices[fuel_type])

    except Exception as e:
        print("Something went wrong during creation of tfrecord files")
        print(e.with_traceback())

    # Close all writers
    for data_set in ['training', 'validation', 'testing']:
        for fuel_type in ['e5', 'e10', 'diesel']:
            print("bla")
            writers[fuel_type + '_' + data_set].close()


if __name__ == '__main__':
    tf.app.run()