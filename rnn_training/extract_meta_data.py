import csv
import progressbar

import tensorflow as tf
import numpy as np
import matplotlib.pyplot as plt

from os import listdir
from os.path import isfile, join
from datetime import datetime

'''
This script looks at all available data from gas stations and extracts certain meta data about it:
- average, maximum number of entries per day and variance from the average over the months
- how are the number of entries distributed over the day
- first global entry, i.e. what is the maximum number of months for all gas stations
'''

# Parse user input
flags = tf.flags
flags.DEFINE_string("data_path", None, "Path of the folder containing prices of gas stations")
FLAGS = flags.FLAGS


# Helper functions
def _parse_str_date(date):
    date += "00"
    return datetime.strptime(date, "%Y-%m-%d %H:%M:%S%z")


# Get all files in directory
if not FLAGS.data_path:
    raise ValueError("You need to specify data_path!")

files = [f for f in listdir(FLAGS.data_path) if isfile(join(FLAGS.data_path, f))]

# Add full path to files
files = [join(FLAGS.data_path, file) for file in files]

''' 
Create new data structures for the results
The following arrays are all of shape (4, 12), where the first index represents the year (starting at 2014)
and the second represents the month in that year. Each also has an according count array in order to be able to average
the results correctly over all gas stations (not every gas station has entries for every month over all the years).
'''

# Array to keep track about the average number of entries per day over the months for all gas stations
all_average_n_entries_per_day = np.zeros(shape=(4, 12), dtype=np.float)

# Array to keep track about the variance from the average number of entries per day over the months for all
# gas stations
all_variance_n_entries_per_day = np.zeros(shape=(4, 12), dtype=np.float)

# Array to keep track about the maximum number of entries per day over the months for all gas stations
all_maximum_n_entries_per_day = np.zeros(shape=(4, 12), dtype=np.float)

# Array to count the number of entries added to the arrays above in order to be able to average them
entry_count = np.zeros(shape=(4, 12), dtype=np.int)


# Not directly connected to arrays above: Keep track about how the number of entries is distributed over the day
daily_distribution = np.zeros(24)


''' Iterate over all available files and extract information'''

# Show progress of the loop
with progressbar.ProgressBar(max_value=len(files)) as bar:
    # Iterate over files
    print("Start iterating over files")
    for index, file in enumerate(files):
        # Open file and parse input
        entries = []
        with open(file, "r") as csvfile:
            reader = csv.reader(csvfile, delimiter=';')
            for row in reader:
                entries.append(row)

        # For each day keep track about the number of entries
        n_entries_per_day = np.zeros(shape=(4, 12, 31), dtype=np.int)

        # Iterate over all entries
        for entry in entries:
            # Get current date
            date = _parse_str_date(entry[0])
            
            # Add this to the result arrays
            n_entries_per_day[date.year - 2014][date.month - 1][date.day - 1] += 1
            daily_distribution[date.hour] += 1

        # Now add this information to the overall result arrays above
        for year in range(4):
            for month in range(12):
                # Calculate the average number of entries per day (consider only days that have entries)
                # Also keep track of the maximum amount of entries
                average = 0
                count = 0
                maximum = 0
                for day in range(31):
                    if n_entries_per_day[year][month][day] != 0:
                        average += n_entries_per_day[year][month][day]
                        count += 1
                        if n_entries_per_day[year][month][day] >= maximum:
                            maximum = n_entries_per_day[year][month][day]
                # If there are no entries for this month, continue with next month
                if count == 0:
                    continue
                average /= count

                # Calculate the variance
                variance = 0
                for day in range(31):
                    if n_entries_per_day[year][month][day] != 0:
                        variance += (n_entries_per_day[year][month][day] - average)

                variance /= count

                # Last, add this information to the global result arrays above (naturally only, if there are entries)
                if count > 0:
                    all_average_n_entries_per_day[year][month] += average
                    all_variance_n_entries_per_day[year][month] += variance
                    all_maximum_n_entries_per_day[year][month] += maximum
                    entry_count[year][month] += 1

        # Update progress bar
        bar.update(index)

''' Now, that all files are evaluated, average the results for all gas stations'''

# Iterate over all years and months
for year in range(4):
    for month in range(12):
        # Only calculate if there actually are entries (avoid zero division)
        if entry_count[year][month] > 0:
            all_average_n_entries_per_day[year][month] /= entry_count[year][month]
            all_variance_n_entries_per_day[year][month] /= entry_count[year][month]
            all_maximum_n_entries_per_day[year][month] /= entry_count[year][month]
daily_distribution /= np.sum(daily_distribution)

''' Print results '''
plt.figure(1)
plt.subplot(311)
plt.plot(all_average_n_entries_per_day.reshape([-1]))
plt.ylabel('Average')

plt.subplot(312)
plt.plot(all_variance_n_entries_per_day.reshape([-1]))
plt.ylabel('Variance')

plt.subplot(313)
plt.plot(all_maximum_n_entries_per_day.reshape([-1]))
plt.ylabel('Maximum')

plt.figure(2)
plt.plot(daily_distribution)
plt.show()
