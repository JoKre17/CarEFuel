import re
import sys
from datetime import datetime

dumpFileName = "history.dump"

if len(sys.argv) != 2:
	print "Date not given as Argument. (YYYY-MM-DD)"
	sys.exit(1)

importDate = sys.argv[1]
date = datetime.strptime(importDate, "%Y-%m-%d")
print "ImportDate: " + str(date)

newDumpFile = open("reduced_" + importDate + ".dump", 'w')

pattern = '\d+\s+[\w\d-]+\s+[\d]+\s+[\d]+\s+[\d]+\s+([\d-]+)\s[\d:+]+\s+[\d]+'

dump= "\n" \
	+ "--" + "\n" \
	+ "-- Data for Name: gas_station_information_history; Type: TABLE DATA; Schema: public; Owner: -" + "\n" \
	+ "--" + "\n" \
	+ "\n" \
	+ "COPY gas_station_information_history (id, stid, e5, e10, diesel, date, changed) FROM stdin;" + "\n"

newDumpFile.write(dump)
newDumpFile.flush()

print "Starting to read dumpFile: " + dumpFileName
count = 0
dump = ""
with open(dumpFileName) as infile:
    for line in infile:
	count += 1

	if(count%1000000 == 0):
		print count

	match = re.match(pattern, line)
	if match is not None:
		if match.group(1) > importDate:
			dump += match.group(0) + "\n"
			if (count % 100000 == 0):
				newDumpFile.write(dump)
				newDumpFile.flush()
				dump = ""
print count

newDumpFile.write("\\.\n")
print "Finished."
