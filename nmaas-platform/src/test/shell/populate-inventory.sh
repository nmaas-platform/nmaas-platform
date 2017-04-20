#!/bin/bash

API_URL=http://localhost:9000/platform/api
echo $API_URL
echo
echo Adding default VPN config for router on the cloud side
curl -X POST $API_URL/management/vpnconfigs/cloud/docker-host-1 --header "Authorization: Basic bm1hYXNUZXN0OnNmRiQjNGZ3YkVl" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/vpnconfigs/cloud-side-router-vpn-config-docker-host-1.json
echo
curl -X GET $API_URL/management/vpnconfigs/cloud --header "Authorization: Basic bm1hYXNUZXN0OnNmRiQjNGZ3YkVl"
echo
echo Adding default VPN config for router on customer 1 -admin- side
curl -X POST $API_URL/management/vpnconfigs/customer/1 --header "Authorization: Basic bm1hYXNUZXN0OnNmRiQjNGZ3YkVl" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/inventory/vpnconfigs/customer-side-router-vpn-config-client-1.json
echo
curl -X GET $API_URL/management/vpnconfigs/customer --header "Authorization: Basic bm1hYXNUZXN0OnNmRiQjNGZ3YkVl"
