#!/bin/bash
DIR=/home/mgmt/platform
echo "Launching NMaaS Platform system tests."
cd $DIR/init-and-tests
./test.sh > test.output
echo "Tests completed (logs: $DIR/init-and-tests/tests.output)"
