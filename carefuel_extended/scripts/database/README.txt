#To setup the database, simply use psql -h hostname -p port -U db_user -f file_to_execute.sql
#This may be as following
	psql -h localhost -p 5432 -U postgres -f create_database_from_scratch.sql

#Notice, that beforehand there have to be an installed postgres 9.4 instance