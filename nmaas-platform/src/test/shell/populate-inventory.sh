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
echo Adding default Docker Hosts
curl -X POST $API_URL/management/dockerhosts --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/dockerhosts/docker-host-1.json
curl -X POST $API_URL/management/dockerhosts --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/dockerhosts/docker-host-2.json
curl -X POST $API_URL/management/dockerhosts --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/dockerhosts/docker-host-3.json
echo
curl -X GET $API_URL/management/dockerhosts --header "Authorization: Bearer $TOKEN" | python -m json.tool

echo
echo Adding default Docker Host attachment points
curl -X POST $API_URL/management/network/dockerhosts --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/dockerhosts/docker-host-1-attach-point.json
echo
curl -X GET $API_URL/management/network/dockerhosts --header "Authorization: Bearer $TOKEN" | python -m json.tool

echo
echo Adding default Kubernetes cluster
curl -X POST $API_URL/management/kubernetes --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/kubernetes/kubernetes-1.json
echo
curl -X GET $API_URL/management/kubernetes --header "Authorization: Bearer $TOKEN" | python -m json.tool

echo
echo Adding default GitLab configuration
curl -X POST $API_URL/management/gitlab --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/gitlab/gitlab-1.json
echo
curl -X GET $API_URL/management/gitlab --header "Authorization: Bearer $TOKEN" | python -m json.tool

echo
echo Adding default Shibboleth configuration
curl -X POST $API_URL/management/shibboleth --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/shibboleth/shibboleth-config.json
echo
curl -X GET $API_URL/management/shibboleth --header "Authorization: Bearer $TOKEN" | python -m json.tool

echo
echo Adding default network attachment point to default domain testdom1
curl -X POST $API_URL/management/domains/domain1/network --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/domains/domain1-network-attach-point.json
echo
curl -X GET $API_URL/management/domains/domain1/network --header "Authorization: Bearer $TOKEN" | python -m json.tool
echo
