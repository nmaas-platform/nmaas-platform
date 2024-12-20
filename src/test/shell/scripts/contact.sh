#!/bin/bash

for CONTACT_FORM in ../data/contact_forms/*.json; do
	curl -X PUT $API_URL/mail/type --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @$CONTACT_FORM
done
