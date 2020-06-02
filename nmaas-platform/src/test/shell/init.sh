#!/bin/bash

function getToken() {
	python -c "import json,sys;sys.stdout.write(json.dumps(json.load(sys.stdin)['token']))" | sed -e 's/^"//' -e 's/"$//'
}

API_URL=http://localhost:9000/api
echo $API_URL

TOKEN=`curl -sX POST $API_URL/auth/basic/login --header "Content-Type: application/json" --header "Accept: application/json" -d @data/login.json | getToken`


echo Token: 
echo ----------------------
echo $TOKEN
echo ----------------------
echo Ping
curl -X GET $API_URL/auth/basic/ping --header "Authorization: Bearer $TOKEN"

echo
echo Add Domain One with codename domain1
curl -X POST $API_URL/domains --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/domains/domain1.json
echo Add Domain Two with codename domain2
curl -X POST $API_URL/domains --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/domains/domain2.json
echo
curl -X GET $API_URL/domains --header "Authorization: Bearer $TOKEN" | python -m json.tool
echo
echo Default mail template
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/mails/html-template/template.html;type=text/html" $API_URL/mail/templates/html
echo
echo Create mail templates
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/activateAccountMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/appDeployedMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/blockAccountMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/externalServiceHealthCheckMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/passwordReset.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/registrationMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/contactFormMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/appActiveMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/appDeletedMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/appNewMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/appNotActiveMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/appRejectedMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/newSsoLoginMail.json
echo
curl -X POST $API_URL/mail/templates --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/mails/broadcast.json
echo

echo
echo App1
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app01-librenms.json
echo
echo App1 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/librenms.svg;type=image/svg+xml" $API_URL/apps/1/logo
echo
echo App1 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/librenms/librenms1.png;type=image/png" $API_URL/apps/1/screenshots
echo 
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/librenms/librenms2.png;type=image/png" $API_URL/apps/1/screenshots

echo
echo App2
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app02-oxidized.json
echo
echo App2 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/oxidized.svg;type=image/svg+xml" $API_URL/apps/2/logo
echo
echo App2 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/oxidized/oxidized1.png;type=image/png" $API_URL/apps/2/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/oxidized/oxidized2.png;type=image/png" $API_URL/apps/2/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/oxidized/oxidized3.png;type=image/png" $API_URL/apps/2/screenshots

echo 
echo App3
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app03-nav.json
echo
echo App3 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/nav.svg;type=image/svg+xml" $API_URL/apps/3/logo
echo
echo App3 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/nav/nav1.png;type=image/png" $API_URL/apps/3/screenshots
echo 
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/nav/nav2.png;type=image/png" $API_URL/apps/3/screenshots
echo 
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/nav/nav3.png;type=image/png" $API_URL/apps/3/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/nav/nav4.png;type=image/png" $API_URL/apps/3/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/nav/nav5.png;type=image/png" $API_URL/apps/3/screenshots

echo
echo App4
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app04-opennti.json
echo
echo App4 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/open-nti.svg;type=image/svg+xml" $API_URL/apps/4/logo
echo
echo App4 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/opennti/opennti1.png;type=image/png" $API_URL/apps/4/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/opennti/opennti2.png;type=image/png" $API_URL/apps/4/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/opennti/opennti3.png;type=image/png" $API_URL/apps/4/screenshots

echo
echo App5
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app05-prometheus.json
echo
echo App5 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/prometheus.svg;type=image/svg+xml" $API_URL/apps/5/logo
echo
echo App5 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/prometheus/prometheus1.png;type=image/png" $API_URL/apps/5/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/prometheus/prometheus2.png;type=image/png" $API_URL/apps/5/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/prometheus/prometheus3.png;type=image/png" $API_URL/apps/5/screenshots
echo

echo
echo App6
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app06-grafana.json
echo
echo App6 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/grafana.svg;type=image/svg+xml" $API_URL/apps/6/logo
echo
echo App6 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/grafana/grafana1.png;type=image/png" $API_URL/apps/6/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/grafana/grafana2.png;type=image/png" $API_URL/apps/6/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/grafana/grafana3.png;type=image/png" $API_URL/apps/6/screenshots
echo

echo
echo App7
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app07-bastion.json
echo
echo App7 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/bastion.svg;type=image/svg+xml" $API_URL/apps/7/logo
echo
echo App7 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/bastion/bastion1.png;type=image/png" $API_URL/apps/7/screenshots
echo

echo
echo App8
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app08-perfsonar-pwa.json
echo
echo App8 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/perfsonar.png;type=image/png" $API_URL/apps/8/logo
echo
echo App8 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/perfsonar-pwa/perfsonar-pwa1.png;type=image/png" $API_URL/apps/8/screenshots
echo
echo App8 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/perfsonar-pwa/perfsonar-pwa2.png;type=image/png" $API_URL/apps/8/screenshots
echo
echo App8 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/perfsonar-pwa/perfsonar-pwa3.png;type=image/png" $API_URL/apps/8/screenshots
echo
echo App8 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/perfsonar-pwa/perfsonar-pwa4.png;type=image/png" $API_URL/apps/8/screenshots
echo
echo App8 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/perfsonar-pwa/perfsonar-pwa5.png;type=image/png" $API_URL/apps/8/screenshots
echo

echo
echo App9
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app09-booked.json
echo
echo App9 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/booked.png;type=image/png" $API_URL/apps/9/logo
echo
echo App9 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/booked/booked1.png;type=image/png" $API_URL/apps/9/screenshots
echo
echo App9 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/booked/booked2.png;type=image/png" $API_URL/apps/9/screenshots
echo
echo App9 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/booked/booked3.png;type=image/png" $API_URL/apps/9/screenshots
echo

echo
echo App10
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app10-spa-inventory.json
echo
echo App10 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/geant.png;type=image/png" $API_URL/apps/10/logo
echo
echo App10 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/spa-inventory/spa-inventory1.png;type=image/png" $API_URL/apps/10/screenshots
echo
echo App10 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/spa-inventory/spa-inventory2.png;type=image/png" $API_URL/apps/10/screenshots
echo
echo App10 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/spa-inventory/spa-inventory3.png;type=image/png" $API_URL/apps/10/screenshots
echo
echo App10 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/spa-inventory/spa-inventory4.png;type=image/png" $API_URL/apps/10/screenshots
echo

echo
echo App11
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app11-statping.json
echo
echo App11 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/statping.png;type=image/png" $API_URL/apps/11/logo
echo
echo App11 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/statping/statping1.png;type=image/png" $API_URL/apps/11/screenshots
echo
echo App11 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/statping/statping2.png;type=image/png" $API_URL/apps/11/screenshots
echo
echo App11 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/statping/statping3.png;type=image/png" $API_URL/apps/11/screenshots
echo

echo
echo App12
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app12-perfsonar-maddash.json
echo
echo App12 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/perfsonar.png;type=image/png" $API_URL/apps/12/logo
echo
echo App12 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/perfsonar-maddash/perfsonar-maddash1.png;type=image/png" $API_URL/apps/12/screenshots
echo

echo
echo App13
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app13-debian-repository.json
echo
echo App13 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/debian.svg;type=image/svg+xml" $API_URL/apps/13/logo
echo
echo App13 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/debian-repository/debian-repository1.png;type=image/png" $API_URL/apps/13/screenshots
echo
echo App13 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/debian-repository/debian-repository2.png;type=image/png" $API_URL/apps/13/screenshots
echo
echo App13 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/debian-repository/debian-repository3.png;type=image/png" $API_URL/apps/13/screenshots
echo
echo App13 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/debian-repository/debian-repository4.png;type=image/png" $API_URL/apps/13/screenshots
echo

echo
echo App14
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app14-influxdb.json
echo
echo App14 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/influxdb.png;type=image/png" $API_URL/apps/14/logo
echo
echo App14 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/influxdb/influxdb1.png;type=image/png" $API_URL/apps/14/screenshots
echo
echo App14 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/influxdb/influxdb2.png;type=image/png" $API_URL/apps/14/screenshots
echo

echo
echo App15
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app15-jenkins.json
echo
echo App15 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/jenkins.svg;type=image/svg+xml" $API_URL/apps/15/logo
echo
echo App15 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/jenkins/jenkins1.png;type=image/png" $API_URL/apps/15/screenshots
echo

echo
echo App15
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app15-jenkins.json
echo
echo App15 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/jenkins.svg;type=image/svg+xml" $API_URL/apps/15/logo
echo
echo App15 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/jenkins/jenkins1.png;type=image/png" $API_URL/apps/15/screenshots
echo

echo
echo ---------------------
echo Get all apps
curl -X GET $API_URL/apps --header "Authorization: Bearer $TOKEN"

echo 
echo ---------------------
echo Add comments to first app
curl -X POST $API_URL/apps/1/comments --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/comments/app1-comment1.json
echo 
curl -X POST $API_URL/apps/1/comments --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/comments/app1-comment2.json
echo 
curl -X POST $API_URL/apps/1/comments --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/comments/app1-comment1-sub1.json

echo 
echo ---------------------
echo Get second app
curl -X GET $API_URL/apps/2/ --header "Authorization: Bearer $TOKEN"


echo 
echo ---------------------
echo Get comments for first app
curl -X GET $API_URL/apps/1/comments --header "Authorization: Bearer $TOKEN"


echo 
echo ---------------------
echo Rate App1
curl -X POST $API_URL/apps/1/rate/my/4 --header "Authorization: Bearer $TOKEN" 

echo 
curl -X GET $API_URL/apps/1/rate --header "Authorization: Bearer $TOKEN" 
echo 
curl -X GET $API_URL/apps/1/rate/my --header "Authorization: Bearer $TOKEN" 
echo 
curl -X GET $API_URL/apps/1/rate/user/1 --header "Authorization: Bearer $TOKEN" 


echo 
echo Rate App2
curl -X POST $API_URL/apps/2/rate/my/4 --header "Authorization: Bearer $TOKEN"

echo 
echo Rate App3
curl -X POST $API_URL/apps/3/rate/my/5 --header "Authorization: Bearer $TOKEN"

echo
echo Rate App4
curl -X POST $API_URL/apps/4/rate/my/4 --header "Authorization: Bearer $TOKEN"

echo 
echo ---------------------
echo Tags:
curl -X GET $API_URL/tags --header "Authorization: Bearer $TOKEN" --header "Accept: application/json"

echo 
echo ---------------------
echo By tag:
curl -X GET $API_URL/tags/management --header "Authorization: Bearer $TOKEN" --header "Accept: application/json"

echo 
echo ---------------------
echo Create app1 aubscription to Domain One
curl -X POST $API_URL/subscriptions --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/subscriptions/sub1.json
echo
echo Create app2 aubscription to Domain One
curl -X POST $API_URL/subscriptions --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/subscriptions/sub2.json
echo
echo Create app3 aubscription to Domain One
curl -X POST $API_URL/subscriptions --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/subscriptions/sub3.json
echo
echo Create app3 aubscription to Domain Two
curl -X POST $API_URL/subscriptions --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/subscriptions/sub4.json
echo
echo Get all subscriptions
curl -X GET $API_URL/subscriptions --header "Authorization: Bearer $TOKEN" --header "Accept: application/json" | python -m json.tool


echo
echo ---------------------
echo Create english language content
curl -X POST $API_URL/i18n/en?enabled=true --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/i18n/en.json
echo
echo Create french language content
curl -X POST $API_URL/i18n/fr?enabled=true --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/i18n/fr.json
echo
echo Create polish language content
curl -X POST $API_URL/i18n/pl?enabled=true --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/i18n/pl.json
echo
echo Create german language content
curl -X POST $API_URL/i18n/de?enabled=true --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/i18n/de.json
