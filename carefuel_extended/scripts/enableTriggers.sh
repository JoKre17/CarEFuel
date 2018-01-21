#!/bin/bash

psql postgresql://postgres:NJuJh1A!Wln..@localhost/carefuel << EOF

ALTER TABLE gas_station_information_history ENABLE TRIGGER ALL;
ALTER TABLE gas_station_information_prediction ENABLE TRIGGER ALL;
ALTER TABLE gas_station ENABLE TRIGGER ALL;

EOF
