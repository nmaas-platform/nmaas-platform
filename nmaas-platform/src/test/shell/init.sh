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
echo Register User First with username user1
curl -X POST $API_URL/auth/basic/registration --header "Content-Type: application/json" --header "Accept: application/json" -d @data/users/user1.json
echo
echo Enable User First
curl -X PUT $API_URL/users/status/2?enabled=true --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json"
echo
echo Set User First an ADMIN role on Domain One
curl -X POST $API_URL/domains/2/users/2/roles --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/users/user1-admin-role.json
echo
echo Register User Second with username user2
curl -X POST $API_URL/auth/basic/registration --header "Content-Type: application/json" --header "Accept: application/json" -d @data/users/user2.json
echo
echo Enable User Second
curl -X PUT $API_URL/users/status/3?enabled=true --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json"
echo
echo Set User Second an USER role on Domain One
curl -X POST $API_URL/domains/2/users/3/roles --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/users/user2-user-role.json
echo
echo Register Operator with username operator
curl -X POST $API_URL/auth/basic/registration --header "Content-Type: application/json" --header "Accept: application/json" -d @data/users/user3.json
echo
echo Enable Operator
curl -X PUT $API_URL/users/status/4?enabled=true --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json"
echo
echo Set Operator an OPERATOR role on Global Domain
curl -X POST $API_URL/domains/1/users/4/roles --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/users/user3-operator-role.json
echo
echo Get all users
curl -X GET $API_URL/users --header "Authorization: Bearer $TOKEN" --header "Accept: application/json" | python -m json.tool
echo

echo
echo App1
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app1-librenms.json
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
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app2-oxidized.json
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
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app3-nav.json
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
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app4-opennti.json
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
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app5-prometheus.json
echo
echo App5 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/prometheus.svg;type=image/svg+xml" $API_URL/apps/5/logo
echo
echo App5 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/prometheus/prometheus_1.png;type=image/png" $API_URL/apps/5/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/prometheus/prometheus_2.png;type=image/png" $API_URL/apps/5/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/prometheus/prometheus_3.png;type=image/png" $API_URL/apps/5/screenshots
echo

echo
echo App6
curl -X POST $API_URL/apps --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/apps/app6-grafana.json
echo
echo App6 logo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/logo/grafana.svg;type=image/svg+xml" $API_URL/apps/6/logo
echo
echo App6 screenshots
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/grafana/grafana_1.png;type=image/png" $API_URL/apps/6/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/grafana/grafana_2.png;type=image/png" $API_URL/apps/6/screenshots
echo
curl -X POST --header "Authorization: Bearer $TOKEN" -F "file=@data/apps/images/screenshots/grafana/grafana_3.png;type=image/png" $API_URL/apps/6/screenshots
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
curl -X POST $API_URL/i18n/en --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/i18n/en.json
echo
echo Create french language content
curl -X POST $API_URL/i18n/fr --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/i18n/fr.json
echo
echo Create polish language content
curl -X POST $API_URL/i18n/pl --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/i18n/pl.json
echo
echo Create german language content
curl -X POST $API_URL/i18n/de --header "Authorization: Bearer $TOKEN" --header "Content-Type: application/json" --header "Accept: application/json" -d @data/i18n/de.json
