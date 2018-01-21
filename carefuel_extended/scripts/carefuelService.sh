#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

pid=$(pgrep java)

function help(){
	echo "Usage: ./carefuelService.sh [start|stop]"
	echo "   start - will stop server if necessary and start again"
	echo "   stop  - stops the server"
}

function stop(){
	pid=$(pgrep java)

	if [ -n "$pid" ]
	then
	        echo "Stopping the server."
	        sudo kill $pid
	else
		echo "Server was not running."
	fi

	return $?
}

function start(){
	echo running $DIR/CarEFuel_Extended_0.2.jar in background.
        nohup sudo java -jar -Xms3g -Xmx3g -Dserver.port=443  -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts -Djavax.net.ssl.trustStorePassword=changeit $DIR/CarEFuel_Extended_0.2.jar &
}

if [ "$#" -gt 0 ]
then
	case $1 in
	start)
	  stop
	  
	  if [[ $? -ne 0 ]]
	  then
	        echo "Did not shut down server properly."
	  else
		start
	  fi
	  
	  ;;
	stop)
	  stop
	  ;;
	*)
	  help
	  ;;
	esac
else
	help
fi



