#!/bin/bash
DIR=/home/mgmt/portal
FILE=$(ls $DIR/ | grep zip)
cd $DIR
unzip -o $FILE
cp $DIR/config/config.json $DIR/
nohup http-server -p 9009 -s --cors > $DIR/nmaas-portal.log 2> $DIR/Error.err < /dev/null &
