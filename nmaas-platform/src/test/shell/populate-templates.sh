#!/bin/bash

function getToken() {
	python -c "import json,sys;sys.stdout.write(json.dumps(json.load(sys.stdin)['token']))" | sed -e 's/^"//' -e 's/"$//'
}

API_URL=http://localhost:9000/api
echo Base API URL $API_URL

TOKEN=`curl -sX POST $API_URL/auth/basic/login --header "Content-Type: application/json" --header "Accept: application/json" -d @data/login.json | getToken`


echo Token:
echo ----------------------
echo $TOKEN
echo ----------------------
echo Ping
curl -X GET $API_URL/auth/basic/ping --header "Authorization: Bearer $TOKEN"

echo
echo Adding default configuration file templates
curl -X POST $API_URL/management/configurations/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/configurations/app1-template1.json
curl -X POST $API_URL/management/configurations/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/configurations/app2-template1.json
curl -X POST $API_URL/management/configurations/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/configurations/app2-template2.json
curl -X POST $API_URL/management/configurations/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/configurations/app4-template1.json
curl -X POST $API_URL/management/configurations/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/configurations/app4-template2.json
echo
curl -X GET $API_URL/management/configurations/templates --header "Authorization: Bearer $TOKEN" | python -m json.tool

echo
echo Adding default docker compose file templates for app1, app2, app3 and app4
curl -X POST $API_URL/management/apps/1/dockercompose/template --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/dockercompose/app1-template1.json
curl -X POST $API_URL/management/apps/2/dockercompose/template --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/dockercompose/app2-template1.json
curl -X POST $API_URL/management/apps/3/dockercompose/template --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/dockercompose/app3-template1.json
curl -X POST $API_URL/management/apps/4/dockercompose/template --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/dockercompose/app4-template1.json
echo
curl -X GET $API_URL/management/apps/1/dockercompose/template --header "Authorization: Bearer $TOKEN" | python -m json.tool
curl -X GET $API_URL/management/apps/2/dockercompose/template --header "Authorization: Bearer $TOKEN" | python -m json.tool
curl -X GET $API_URL/management/apps/3/dockercompose/template --header "Authorization: Bearer $TOKEN" | python -m json.tool
curl -X GET $API_URL/management/apps/4/dockercompose/template --header "Authorization: Bearer $TOKEN" | python -m json.tool

echo
echo Adding default kubernetes template for app2
curl -X POST $API_URL/management/apps/2/kubernetes/template --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/templates/kubernetes/app2-template1.json
echo
curl -X GET $API_URL/management/apps/2/kubernetes/template --header "Authorization: Bearer $TOKEN" | python -m json.tool
