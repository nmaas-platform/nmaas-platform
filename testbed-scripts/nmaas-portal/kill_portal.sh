#!/bin/bash
DIR=/home/mgmt/portal
COMMAND=$(ps aux | grep http-server | grep -v grep | awk '{print $2}')
if [ -z "$COMMAND" ]
then
    echo "nmaas-portal was not running"
else 
    nohup kill $COMMAND > $DIR/nmaas-portal.log 2> $DIR/Error.err < /dev/null
fi
