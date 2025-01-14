#!/bin/bash

for LANGUAGE in ../data/i18n/*.json; do
	LANG=$(basename "$LANGUAGE" .${LANGUAGE##*.})
        curl -X POST $API_URL/i18n/$LANG?enabled=true --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @$LANGUAGE
done
