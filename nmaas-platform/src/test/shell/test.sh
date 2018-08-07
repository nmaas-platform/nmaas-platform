#!/bin/bash

function getToken() {
        python -c "import json,sys;sys.stdout.write(json.dumps(json.load(sys.stdin)['token']))" | sed -e 's/^"//' -e 's/"$//'
}

API_URL=http://localhost:9001/api
echo $API_URL

TOKEN=`curl -sX POST $API_URL/auth/basic/login --header "Content-Type: application/json" --header "Accept: application/json" -d @data/login.json | getToken`

echo
echo ---------------------
echo "Create app2 (Oxidized) instance"
curl -X POST $API_URL/apps/instances --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/instances/instance1.json

while [ true ]
do
sleep 5
echo ---------------------
echo Get app instance 1 state
OUTPUT=`curl -X GET $API_URL/apps/instances/1/state --header "Authorization: Bearer $TOKEN" --header "Accept: application/json" 2> /dev/null`
echo $OUTPUT
if [[ "$OUTPUT" == *"CONFIGURATION_AWAITING"* ]]; then
break
fi
if [[ "$OUTPUT" == *"FAILURE"* ]]; then
exit 13
fi
done
echo
echo "Management VPN configured"
echo
echo Applying app instance 1 user configuration
OUTPUT=`curl -X POST $API_URL/apps/instances/1/configure --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" -d @data/apps/instances/instance1-config.json`
echo $OUTPUT
echo
while [ true ]
do
sleep 5
echo ---------------------
echo Get app instance 1 state
OUTPUT=`curl -X GET $API_URL/apps/instances/1/state --header "Authorization: Bearer $TOKEN" --header "Accept: application/json" 2> /dev/null`
echo $OUTPUT
if [[ "$OUTPUT" == *"RUNNING"* ]]; then
break
fi
if [[ "$OUTPUT" == *"FAILURE"* ]]; then
exit 13
fi
done
echo
echo "Application is running"
echo
curl -X GET http://10.134.250.1:1000/nodes
sleep 5
echo Removing app instance 1
OUTPUT=`curl -X DELETE $API_URL/apps/instances/1 --header "Authorization: Bearer $TOKEN"`
echo $OUTPUT
echo
while [ true ]
do
sleep 5
echo ---------------------
echo Get app instance 1 state
OUTPUT=`curl -X GET $API_URL/apps/instances/1/state --header "Authorization: Bearer $TOKEN" --header "Accept: application/json" 2> /dev/null`
echo $OUTPUT
if [[ "$OUTPUT" == *"DONE"* ]]; then
break
fi
if [[ "$OUTPUT" == *"FAILURE"* ]]; then
exit 13
fi
done