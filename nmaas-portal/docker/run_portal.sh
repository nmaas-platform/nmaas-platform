#!/bin/sh
DIR=/usr/share/nginx/html
FILE=$(ls $DIR/ | grep zip)
cd $DIR
unzip -o $FILE

if [[ -z "${API_URL}" ]]; then
  cp $DIR/config/config.default.json $DIR/config.json
else
  envsubst < $DIR/config/config.template.json > $DIR/config.json
fi

exec nginx -g 'daemon off;'
