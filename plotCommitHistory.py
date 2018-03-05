#!/bin/env python
import os
import datetime
import matplotlib.dates as pydates
import matplotlib.pyplot as plt

commitMap = {}
#--format=%at
stream = os.popen("git log --no-merges  --numstat")

# datetime.datetime.strptime('Wed Feb 21 01:13:52 2018 +0100', "%a %b %d %H:%M:%S %Y %z")

time = ""

threshold = 100
max_commit_size = 1000
currentAuthor = ''
added = 0

SKIP_FIRST_LINE = True
for line in stream:
	

		
	if 'Author:' in line:
		spA = line.split(' ')
		author = ''
		for e in spA:
			if '@' in e:
				author = e
		currentAuthor = author.split('@')[0].replace('<', '').replace('.', ' ').title()
		if currentAuthor not in commitMap:
			commitMap[currentAuthor] = {'dates': [], 'values': []}
	
	if 'Date:' in line:
		time = datetime.datetime.strptime(line[8:].strip(), "%a %b %d %H:%M:%S %Y %z")
	
	sp = line.split("\t")
	if len(sp) == 3:
		try:
			size = int(sp[0])
			if size < threshold:
				added = added + size
		except (ValueError):
			pass
			
	# collect into map
	if 'commit' in line and len(line.split(' ')) == 2:
		if SKIP_FIRST_LINE:
			SKIP_FIRST_LINE = False
			continue
		
		if added >= 5 and added <= max_commit_size:
			print(str(time) + " " + str(added))
			commitMap[currentAuthor]['dates'].append(time)
			commitMap[currentAuthor]['values'].append(added)

		added = 0

if added >= 5 and added <= max_commit_size:
	print(str(time) + " " + str(added))
	commitMap[currentAuthor]['dates'].append(time)
	commitMap[currentAuthor]['values'].append(added)

allDates = []
allValues = []
for author in commitMap:
	if any(lastName in author.lower() for lastName in ['gritz', 'kriegel', 'nommensen', 'wallat']):
		allDates.extend(pydates.date2num(commitMap[author]['dates']))
		allValues.extend(commitMap[author]['values'])
		#plt.plot_date(pydates.date2num(commitMap[author]['dates']), commitMap[author]['values'], label=author)
	
plt.plot_date(allDates, allValues)
fig = plt.gcf()
fig.autofmt_xdate()

#plt.legend(loc='upper right')
plt.show()