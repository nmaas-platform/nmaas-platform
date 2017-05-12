#!/bin/bash
DIR=/home/mgmt/platform
COMMAND=$(ps aux | grep nmaas-platform | grep -v grep | awk '{print $2}')
if [ -z "$COMMAND" ]
then
    echo "nmaas-platform was not running"
else
    nohup kill $COMMAND > $DIR/nohup-temp/temp.out 2> $DIR/nohup-temp/temp.err < /dev/null
    rm $DIR/nohup-temp/temp.out
    rm $DIR/nohup-temp/temp.err
    rm $DIR/init-and-tests/init.output
    sudo rm -r /tmp/*
    if (( "$#" == 1 ))
    then
        echo "Removing containers and networks"
        /home/mgmt/scripts/clear_docker_hosts.sh 10.134.250.1
    fi
fi
