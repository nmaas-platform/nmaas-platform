#!/bin/bash

API_URL=http://localhost:9000/platform/api
echo $API_URL
echo
echo Adding default Docker Host attachment points
curl -X POST $API_URL/management/network/dockerhosts --header "Authorization: Basic bm1hYXNUZXN0OnNmRiQjNGZ3YkVl" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/network/docker-host-1-attach-point.json
echo
curl -X GET $API_URL/management/network/dockerhosts --header "Authorization: Basic bm1hYXNUZXN0OnNmRiQjNGZ3YkVl"
echo
echo Adding default customer 1 -admin- network attachment points
curl -X POST $API_URL/management/network/customernetworks --header "Authorization: Basic bm1hYXNUZXN0OnNmRiQjNGZ3YkVl" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/network/customer-1-network-attach-point.json
echo
curl -X GET $API_URL/management/network/customernetworks --header "Authorization: Basic bm1hYXNUZXN0OnNmRiQjNGZ3YkVl"
echo