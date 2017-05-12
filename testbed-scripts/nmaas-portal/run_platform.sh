#!/bin/bash
DIR=/home/mgmt/platform
FILE=$(ls $DIR | grep nmaas-platform-)
cd $DIR
nohup java -jar $FILE --spring.config.name=nmaas-platform > nohup-temp/temp.out 2> nohup-temp/temp.err < /dev/null &
echo "NMaaS Platform is booting in backgroud"
echo "Waiting for 25 seconds before launching NMaaS Platform initialization"
sleep 25
cd init-and-tests
./init.sh > init.output
./populate-inventory.sh >> init.output
echo "Initialization completed (logs: $DIR/init-and-tests/init.output)"
