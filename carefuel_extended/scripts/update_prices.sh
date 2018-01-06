#!/bin/bash

# This script automatically downloads the newest gas station prices from
# Tankerkoenig.de and updates the local PSQL database. Afterwards it sends a
# message to the local Spring webserver, telling it to update its price
# predictions for the next month. For that, the server should listen on
# port 50001 and react to incoming TCP messages with content "update".
#
# This script is intended to be run regulary, i.e. once a day.
#
# Important: This script needs to be run as the user postgres!

# Download the newest database database dump into /tmp and extract
cd /tmp
curl https://creativecommons.tankerkoenig.de/history/history.dump.gz > history.dump.gz
gunzip history.dump.gz

# Load newest data into database
psql carefuel < history.dump

# Clean up
rm history.dump

# Notify the local webserver to update prices
echo "update" | netcat 127.0.0.1
