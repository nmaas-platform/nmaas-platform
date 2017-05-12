#!/bin/bash
DIR=/home/mgmt/platform
FILE=$(ls $DIR | grep nmaas-platform-)
cd $DIR
nohup java -jar $FILE --spring.config.name=nmaas-platform > nohup-temp/temp.out 2> nohup-temp/temp.err < /dev/null &
