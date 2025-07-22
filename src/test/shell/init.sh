#!/bin/bash

function getToken() {
    python -c "import json,sys;sys.stdout.write(json.dumps(json.load(sys.stdin)['token']))" | sed -e 's/^"//' -e 's/"$//'
}

echo "Waiting for nmaas Platform initialization"
until $(curl --output /dev/null --silent --head --fail http://localhost:9000/actuator/health); do
    echo "..."
    sleep 1s
done

export API_URL=http://localhost:9000/api

echo nmaas API: $API_URL
echo ----------------------

export TOKEN=`curl -sX POST $API_URL/auth/basic/login --header "Content-Type: application/json" --header "Accept: application/json" -d "$(envsubst < ./data/login.json)" | getToken`
echo Token: $TOKEN
echo ----------------------

echo .
echo Performing a ping check
curl -X GET $API_URL/auth/basic/ping --header "Authorization: Bearer $TOKEN"

echo .
echo Notifying about init scripts execution started
curl -X POST $API_URL/init/started --header "Authorization: Bearer $TOKEN"

cd scripts

echo .
echo Adding email templates
./mail.sh

echo .
echo Adding applications
./app.sh

echo .
echo Adding translations
./transl.sh

echo .
echo Adding contact forms
./contact.sh

echo Notifying about init scripts execution completed
curl -X POST $API_URL/init/completed --header "Authorization: Bearer $TOKEN"

echo "Initialization completed"