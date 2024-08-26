@echo off
setlocal enableDelayedExpansion

set API_URL=http://localhost:9000/api
echo %API_URL%

set LF=^




for /f "delims=" %%f in ('curl -sX POST %API_URL%/auth/basic/login --header "Content-Type: application/json" --header "Accept: application/json" -d @data\login.json') do (
REM set "LOGIN="
	if defined LOGIN set "LOGIN=!LOGIN!!LF!"
	set "LOGIN=!LOGIN!%%f"
)

rem echo Output: !LOGIN!
REM ref: http://stackoverflow.com/questions/36374496/parse-simple-json-string-in-batch
set LOGIN=%LOGIN:"=%
echo stage 1 - %LOGIN%
echo ---
set "LOGIN=%LOGIN:~1,-1%"
echo stage 2 - %LOGIN%
echo ---
set "LOGIN=%LOGIN::==%"
echo stage 3 - !LOGIN!
echo ---

FOR /F "delims=," %%a in ("!LOGIN!") do (
  echo Found: %%a
  set "%%a"
  echo --
)

rem echo !LOGIN!

rem set token=%LOGIN%

echo Token: 
echo ----------------------
echo %token%
echo ----------------------
echo Ping
curl -X GET %API_URL%/auth/basic/ping --header "Authorization: Bearer %token%"
echo

echo ---------------------
echo Add Domain One with codename domain1
curl -X POST %API_URL%/domains --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\domains\domain1.json
echo.
echo Add Domain Two with codename domain2
curl -X POST %API_URL%/domains --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\domains\domain2.json
echo.

echo ---------------------
echo Create common mail template
curl -X POST %API_URL%/mail/templates/html --header "Authorization: Bearer %token%" -F "file=@data\mails\html-template\template.html;type=text/html"
echo.
echo Create mail templates
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\activateAccountMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appDeployedMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\blockAccountMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\externalServiceHealthCheckMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\passwordReset.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\registrationMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\contactFormMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appActiveMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appDeletedMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appNewMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appNotActiveMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appRejectedMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\newSsoLoginMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\broadcast.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appDeploymentFailedMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\issueReport.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\newDomainRequest.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appUpgradedMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appUpgradePossibleMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appUpgradeSummaryMail.json
echo.
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\newActiveApp.json
echo.

echo.
echo App1
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app01-prometheus.json
echo.
echo App1 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\prometheus.svg;type=image/svg+xml" %API_URL%/apps/1/logo
echo.
echo App1 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images/screenshots\prometheus\prometheus1.png;type=image/png" %API_URL%/apps/1/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps/images\screenshots\prometheus\prometheus2.png;type=image/png" %API_URL%/apps/1/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps/images\screenshots\prometheus\prometheus3.png;type=image/png" %API_URL%/apps/1/screenshots

echo.
echo App2
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app02-grafana.json
echo.
echo App2 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\grafana.svg;type=image/svg+xml" %API_URL%/apps/2/logo
echo.
echo App2 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\grafana\grafana1.png;type=image/png" %API_URL%/apps/2/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\grafana\grafana2.png;type=image/png" %API_URL%/apps/2/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\grafana\grafana3.png;type=image/png" %API_URL%/apps/2/screenshots

echo.
echo App3
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app03-uptime-kuma.json
echo.
echo App3 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\uptime-kuma.png;type=image/png" %API_URL%/apps/3/logo
echo.
echo App3 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma1.jpg;type=image/jpg" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma2.jpg;type=image/jpg" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma3.jpg;type=image/jpg" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma4.jpg;type=image/jpg" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma5.jpg;type=image/jpg" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma6.jpg;type=image/jpg" %API_URL%/apps/3/screenshots

echo.
echo App4
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app04-healthchecks.json
echo.
echo App4 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\healthchecks.jpg;type=image/jpg" %API_URL%/apps/4/logo
echo.
echo App4 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\healthchecks\healthchecks1.png;type=image/png" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\healthchecks\healthchecks2.png;type=image/png" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\healthchecks\healthchecks3.png;type=image/png" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\healthchecks\healthchecks4.png;type=image/png" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\healthchecks\healthchecks5.png;type=image/png" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\healthchecks\healthchecks6.png;type=image/png" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\healthchecks\healthchecks7.png;type=image/png" %API_URL%/apps/4/screenshots

echo.
echo App5
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app05-postgresql.json
echo.
echo App5 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\postgresql.svg;type=image/svg+xml" %API_URL%/apps/5/logo

echo.
echo ---------------------
echo Activate apps
curl -X PATCH %API_URL%/apps/state/1 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/2 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/3 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/4 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/5 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json

echo.
echo ---------------------
echo Add comments to first app
curl -X POST %API_URL%/apps/1/comments --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\comments\app1-comment1.json
echo.
curl -X POST %API_URL%/apps/1/comments --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\comments\app1-comment2.json
echo.
curl -X POST %API_URL%/apps/1/comments --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\comments\app1-comment1-sub1.json

echo.
echo ---------------------
echo Rate App1
curl -X POST %API_URL%/apps/1/rate/my/4 --header "Authorization: Bearer %token%" 

echo.
curl -X GET %API_URL%/apps/1/rate --header "Authorization: Bearer %token%" 
echo.
curl -X GET %API_URL%/apps/1/rate/my --header "Authorization: Bearer %token%" 
echo.
curl -X GET %API_URL%/apps/1/rate/user/1 --header "Authorization: Bearer %token%" 

echo.
echo Rate App2
curl -X POST %API_URL%/apps/2/rate/my/4 --header "Authorization: Bearer %token%"

echo.
echo Rate App3
curl -X POST %API_URL%/apps/3/rate/my/5 --header "Authorization: Bearer %token%"

echo.
echo Rate App4
curl -X POST %API_URL%/apps/4/rate/my/4 --header "Authorization: Bearer %token%"

echo.
echo ---------------------
echo Tags:
curl -X GET %API_URL%/tags --header "Authorization: Bearer %token%" --header "Accept: application/json"

echo.
echo ---------------------
echo Create app1 subscription to Domain One
curl -X POST %API_URL%/subscriptions --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\subscriptions\sub1.json
echo.
echo Create app2 subscription to Domain One
curl -X POST %API_URL%/subscriptions --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\subscriptions\sub2.json
echo.
echo Create app3 subscription to Domain One
curl -X POST %API_URL%/subscriptions --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\subscriptions\sub3.json
echo.
echo Create app3 subscription to Domain Two
curl -X POST %API_URL%/subscriptions --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\subscriptions\sub4.json
echo.

echo.
echo ---------------------
echo Create english language content
curl -X POST %API_URL%/i18n/en?enabled=true --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\i18n\en.json
echo.
echo Create french language content
curl -X POST %API_URL%/i18n/fr?enabled=true --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\i18n\fr.json
echo.
echo Create polish language content
curl -X POST %API_URL%/i18n/pl?enabled=true --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\i18n\pl.json
echo.
echo Create german language content
curl -X POST %API_URL%/i18n/de?enabled=true --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\i18n\de.json
echo.

echo.
echo ---------------------
echo Insert form type contact
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\contact.json
echo.
echo Insert form type issue
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\issue_report.json
echo.
echo Insert form type feature request
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\feature_request.json
echo.
echo Insert form type access request
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\access_request.json
echo.
echo Insert form type domain request
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\domain_request.json
echo.
