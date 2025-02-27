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
curl --proxy http://web-proxy.rrzn.uni-hannover.de:3128  https://creativecommons.tankerkoenig.de/history/history.dump.gz > history.dump.gz
gunzip history.dump.gz

# Build reduced Dump
dateYesterday=$(date -d "yesterday" '+%Y-%m-%d')
python /carefuel/scripts/diffDump.py $dateYesterday

rm history.dump

# Load newest data into database
psql carefuel < reduced_$dateYesterday.dump

# Clean up
rm reduced_$dateYesterday.dump

# Notify the local webserver to update prices
echo "update" | netcat 127.0.0.1
