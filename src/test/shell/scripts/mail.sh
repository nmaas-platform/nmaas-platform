#!/bin/bash

echo
echo "Default mail template"
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@../data/mails/html-template/template.html;type=text/html" $API_URL/mail/templates/html

echo "Other templates"
for TEMPLATE in ../data/mails/*.json; do
	curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @$TEMPLATE
done
