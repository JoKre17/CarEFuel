#!/bin/bash

psql postgresql://postgres:NJuJh1A!Wln..@localhost/carefuel << EOF

ALTER TABLE gas_station_information_history DISABLE TRIGGER ALL;
ALTER TABLE gas_station_information_prediction DISABLE TRIGGER ALL;
ALTER TABLE gas_station DISABLE TRIGGER ALL;

EOF
