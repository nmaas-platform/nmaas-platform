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
curl -X POST $API_URL/management/dockerhosts --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/dockerhosts/docker-host-1.json
curl -X POST $API_URL/management/dockerhosts --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/dockerhosts/docker-host-2.json
curl -X POST $API_URL/management/dockerhosts --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/dockerhosts/docker-host-3.json
echo
curl -X GET $API_URL/management/dockerhosts --header "Authorization: Bearer $TOKEN"

echo
echo Adding default Docker Host attachment points
curl -X POST $API_URL/management/network/dockerhosts --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/network/docker-host-1-attach-point.json
echo
curl -X GET $API_URL/management/network/dockerhosts --header "Authorization: Bearer $TOKEN"

echo
echo Adding default Kubernetes cluster
curl -X POST $API_URL/management/kubernetes --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/kubernetes/kubernetes-1.json
echo
curl -X GET $API_URL/management/kubernetes --header "Authorization: Bearer $TOKEN"

echo
echo Adding default customer 1 -admin- network attachment points
curl -X POST $API_URL/management/network/customernetworks --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/network/customer-1-network-attach-point.json
echo
curl -X GET $API_URL/management/network/customernetworks --header "Authorization: Bearer $TOKEN"
echo
