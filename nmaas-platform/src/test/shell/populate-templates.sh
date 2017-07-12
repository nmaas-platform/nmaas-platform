#!/bin/bash

function getToken() {
	python -c "import json,sys;sys.stdout.write(json.dumps(json.load(sys.stdin)['token']))" | sed -e 's/^"//' -e 's/"$//'
}

PORTAL_API_URL=http://localhost:9000/portal/api
echo $PORTAL_API_URL

TOKEN=`curl -sX POST $PORTAL_API_URL/auth/basic/login --header "Content-Type: application/json" --header "Accept: application/json" -d @data/login.json | getToken`


echo Token:
echo ----------------------
echo $TOKEN
echo ----------------------
echo Ping
curl -X GET $PORTAL_API_URL/auth/basic/ping --header "Authorization: Bearer $TOKEN"

API_URL=http://localhost:9000/platform/api
echo $API_URL

echo
echo Adding default Docker Hosts
curl -X POST $API_URL/management/configs --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configs\app1-template1.json
curl -X POST $API_URL/management/configs --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configs\app2-template1.json
curl -X POST $API_URL/management/configs --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configs\app2-template2.json
echo
curl -X GET $API_URL/management/configs --header "Authorization: Bearer $TOKEN"