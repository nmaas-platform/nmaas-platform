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
echo Domain1
curl -X POST %API_URL%/domains --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\domains\domain1.json
echo.

echo
echo Domain2
curl -X POST %API_URL%/domains --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\domains\domain2.json
echo.

echo
echo ---------------------
echo
echo Default mail template
curl -X POST %API_URL%/mail/templates/html --header "Authorization: Bearer %token%" -F "file=@data\mails\html-template\template.html;type=text/html"
echo.
echo
echo Create mail templates
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\activateAccountMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appDeployedMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\blockAccountMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\externalServiceHealthCheckMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\passwordReset.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\registrationMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\contactFormMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appActiveMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appDeletedMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appNewMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appNotActiveMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appRejectedMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\newSsoLoginMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\broadcast.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\appDeploymentFailedMail.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\issueReport.json
echo.
echo
curl -X POST %API_URL%/mail/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\mails\newDomainRequest.json
echo.

echo.
echo App1
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app01-librenms.json
echo.
echo App1 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\librenms.svg;type=image/png" %API_URL%/apps/1/logo
echo.
echo App1 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\librenms\librenms1.png;type=image/png" %API_URL%/apps/1/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\librenms\librenms2.png;type=image/png" %API_URL%/apps/1/screenshots

echo.
echo App2
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app02-oxidized.json
echo.
echo App2 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\oxidized.svg;type=image/svg+xml" %API_URL%/apps/2/logo
echo.
echo App2 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\oxidized\oxidized1.png;type=image/png" %API_URL%/apps/2/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\oxidized\oxidized2.png;type=image/png" %API_URL%/apps/2/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\oxidized\oxidized3.png;type=image/png" %API_URL%/apps/2/screenshots

echo.
echo App3
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app03-nav.json
echo.
echo App3 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\nav.svg;type=image/svg+xml" %API_URL%/apps/3/logo
echo.
echo App3 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\nav\nav1.png;type=image/jpg" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\nav\nav2.png;type=image/png" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\nav\nav3.png;type=image/png" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\nav\nav4.png;type=image/png" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\nav\nav5.png;type=image/png" %API_URL%/apps/3/screenshots

echo.
echo App4
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app04-opennti.json
echo.
echo App4 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\open-nti.svg;type=image/svg+xml" %API_URL%/apps/4/logo
echo.
echo App4 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\opennti\opennti1.png;type=image/png" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\opennti\opennti2.png;type=image/png" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\opennti\opennti3.png;type=image/png" %API_URL%/apps/4/screenshots

echo.
echo App5
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app05-prometheus.json
echo.
echo App5 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\prometheus.svg;type=image/svg+xml" %API_URL%/apps/5/logo
echo.
echo App5 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images/screenshots\prometheus\prometheus1.png;type=image/png" %API_URL%/apps/5/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps/images\screenshots\prometheus\prometheus2.png;type=image/png" %API_URL%/apps/5/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps/images\screenshots\prometheus\prometheus3.png;type=image/png" %API_URL%/apps/5/screenshots
echo.

echo.
echo App6
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app06-grafana.json
echo.
echo App6 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\grafana.svg;type=image/svg+xml" %API_URL%/apps/6/logo
echo.
echo App6 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\grafana\grafana1.png;type=image/png" %API_URL%/apps/6/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\grafana\grafana2.png;type=image/png" %API_URL%/apps/6/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\grafana\grafana3.png;type=image/png" %API_URL%/apps/6/screenshots
echo.
echo App6 v2
curl -X POST %API_URL%/apps/version --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app06-grafana_v7.2.0.json
echo.

echo.
echo App7
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app07-bastion.json
echo.
echo App7 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\bastion.svg;type=image/svg+xml" %API_URL%/apps/7/logo
echo.
echo App7 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\bastion\bastion1.png;type=image/png" %API_URL%/apps/7/screenshots
echo.

echo.
echo App8
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app08-perfsonar-pwa.json
echo.
echo App8 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\perfsonar.png;type=image/png" %API_URL%/apps/8/logo
echo.
echo App8 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\perfsonar-pwa\perfsonar-pwa1.png;type=image/png" %API_URL%/apps/8/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\perfsonar-pwa\perfsonar-pwa2.png;type=image/png" %API_URL%/apps/8/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\perfsonar-pwa\perfsonar-pwa3.png;type=image/png" %API_URL%/apps/8/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\perfsonar-pwa\perfsonar-pwa4.png;type=image/png" %API_URL%/apps/8/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\perfsonar-pwa\perfsonar-pwa5.png;type=image/png" %API_URL%/apps/8/screenshots
echo.

echo.
echo App9
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app09-booked.json
echo.
echo App9 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\booked.png;type=image/png" %API_URL%/apps/9/logo
echo.
echo App9 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\booked\booked1.png;type=image/png" %API_URL%/apps/9/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\booked\booked2.png;type=image/png" %API_URL%/apps/9/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\booked\booked3.png;type=image/png" %API_URL%/apps/9/screenshots
echo.

echo.
echo App10
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app10-spa-inventory.json
echo.
echo App10 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\spa.png;type=image/png" %API_URL%/apps/10/logo
echo.
echo App10 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\spa-inventory\spa-inventory1.png;type=image/png" %API_URL%/apps/10/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\spa-inventory\spa-inventory2.png;type=image/png" %API_URL%/apps/10/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\spa-inventory\spa-inventory3.png;type=image/png" %API_URL%/apps/10/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\spa-inventory\spa-inventory4.png;type=image/png" %API_URL%/apps/10/screenshots
echo.

echo.
echo App11
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app11-statping.json
echo.
echo App11 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\statping.png;type=image/png" %API_URL%/apps/11/logo
echo.
echo App11 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\statping\statping1.png;type=image/png" %API_URL%/apps/11/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\statping\statping2.png;type=image/png" %API_URL%/apps/11/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\statping\statping3.png;type=image/png" %API_URL%/apps/11/screenshots
echo.

echo.
echo App12
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app12-perfsonar-maddash.json
echo.
echo App12 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\perfsonar.png;type=image/png" %API_URL%/apps/12/logo
echo.
echo App12 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\perfsonar-maddash\perfsonar-maddash1.png;type=image/png" %API_URL%/apps/12/screenshots
echo.

echo.
echo App13
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app13-debian-repository.json
echo.
echo App13 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\debian.svg;type=image/svg+xml" %API_URL%/apps/13/logo
echo.
echo App13 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\debian-repository\debian-repository1.png;type=image/png" %API_URL%/apps/13/screenshots
echo.
echo App13 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\debian-repository\debian-repository2.png;type=image/png" %API_URL%/apps/13/screenshots
echo.
echo App13 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\debian-repository\debian-repository3.png;type=image/png" %API_URL%/apps/13/screenshots
echo.
echo App13 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\debian-repository\debian-repository4.png;type=image/png" %API_URL%/apps/13/screenshots
echo.

echo.
echo App14
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app14-influxdb.json
echo.
echo App14 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\influxdb.png;type=image/png" %API_URL%/apps/14/logo
echo.
echo App14 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\influxdb\influxdb1.png;type=image/png" %API_URL%/apps/14/screenshots
echo.
echo App14 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\influxdb\influxdb2.png;type=image/png" %API_URL%/apps/14/screenshots
echo.

echo.
echo App15
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app15-jenkins.json
echo.
echo App15 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\jenkins.svg;type=image/svg+xml" %API_URL%/apps/15/logo
echo.
echo App15 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\jenkins\jenkins1.png;type=image/png" %API_URL%/apps/15/screenshots
echo.

echo.
echo App16
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app16-elasticstack.json
echo.
echo App16 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\elk.svg;type=image/svg+xml" %API_URL%/apps/16/logo
echo.
echo App16 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\elasticstack\elk1.png;type=image/png" %API_URL%/apps/16/screenshots
echo.
echo App16 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\elasticstack\elk2.png;type=image/png" %API_URL%/apps/16/screenshots
echo.

echo.
echo App17
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app17-perfsonar-esmond.json
echo.
echo App17 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\perfsonar.png;type=image/png" %API_URL%/apps/17/logo
echo.
echo App17 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\perfsonar-esmond\perfsonar-esmond1.png;type=image/png" %API_URL%/apps/17/screenshots
echo.

echo.
echo App18
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app18-wifimon.json
echo.
echo App18 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\wifimon.png;type=image/png" %API_URL%/apps/18/logo
echo.
echo App18 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\wifimon\wifimon1.png;type=image/png" %API_URL%/apps/18/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\wifimon\wifimon2.png;type=image/png" %API_URL%/apps/18/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\wifimon\wifimon3.png;type=image/png" %API_URL%/apps/18/screenshots
echo.
echo App19 v2
curl -X POST %API_URL%/apps/version --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app18-wifimon_v1.4.0.json
echo.

echo.
echo App19
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app19-perfsonar-centralmanagement.json
echo.
echo App19 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\perfsonar.png;type=image/png" %API_URL%/apps/19/logo
echo.
echo App19 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\perfsonar-centralmanagement\perfsonar-centralmanagement1.png;type=image/png" %API_URL%/apps/19/screenshots
echo.

echo.
echo App20
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app20-icinga.json
echo.
echo App20 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\icinga.svg;type=image/svg+xml" %API_URL%/apps/20/logo
echo.
echo App20 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\icinga\icinga1.png;type=image/png" %API_URL%/apps/20/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\icinga\icinga2.png;type=image/png" %API_URL%/apps/20/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\icinga\icinga3.png;type=image/png" %API_URL%/apps/20/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\icinga\icinga4.png;type=image/png" %API_URL%/apps/20/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\icinga\icinga5.png;type=image/png" %API_URL%/apps/20/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\icinga\icinga6.png;type=image/png" %API_URL%/apps/20/screenshots
echo.

echo.
echo App21
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app21-victoria-metrics.json
echo.
echo App21 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\victoria-metrics.png;type=image/png" %API_URL%/apps/21/logo
echo.
echo App21 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\victoria-metrics\victoria-metrics1.png;type=image/png" %API_URL%/apps/21/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\victoria-metrics\victoria-metrics2.png;type=image/png" %API_URL%/apps/21/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\victoria-metrics\victoria-metrics3.png;type=image/png" %API_URL%/apps/21/screenshots
echo.

echo.
echo App22
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app22-synapse.json
echo.
echo App22 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\synapse.png;type=image/png" %API_URL%/apps/22/logo
echo.
echo App22 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\synapse\synapse1.png;type=image/png" %API_URL%/apps/22/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\synapse\synapse2.png;type=image/png" %API_URL%/apps/22/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\synapse\synapse3.png;type=image/png" %API_URL%/apps/22/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\synapse\synapse4.png;type=image/png" %API_URL%/apps/22/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\synapse\synapse5.png;type=image/png" %API_URL%/apps/22/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\synapse\synapse6.png;type=image/png" %API_URL%/apps/22/screenshots
echo.

echo.
echo App23
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app23-routinator.json
echo.
echo App23 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\routinator.svg;type=image/svg+xml" %API_URL%/apps/23/logo
echo.
echo App23 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\routinator\routinator1.png;type=image/png" %API_URL%/apps/23/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\routinator\routinator2.png;type=image/png" %API_URL%/apps/23/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\routinator\routinator3.png;type=image/png" %API_URL%/apps/23/screenshots
echo.

echo.
echo App24
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app24-codimd.json
echo.
echo App24 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\codimd.png;type=image/png" %API_URL%/apps/24/logo
echo.
echo App24 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\codimd\codimd1.png;type=image/png" %API_URL%/apps/24/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\codimd\codimd2.png;type=image/png" %API_URL%/apps/24/screenshots
echo.

echo.
echo App25
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app25-webdav.json
echo.
echo App25 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\webdav.svg;type=image/svg+xml" %API_URL%/apps/25/logo
echo.
echo App25 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\webdav\webdav1.png;type=image/png" %API_URL%/apps/25/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\webdav\webdav2.png;type=image/png" %API_URL%/apps/25/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\webdav\webdav3.png;type=image/png" %API_URL%/apps/25/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\webdav\webdav4.png;type=image/png" %API_URL%/apps/25/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\webdav\webdav5.png;type=image/png" %API_URL%/apps/25/screenshots
echo.

echo.
echo App26
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app26-uptime-kuma.json
echo.
echo App26 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\uptime-kuma.png;type=image/png" %API_URL%/apps/26/logo
echo.
echo App26 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma1.jpg;type=image/jpg" %API_URL%/apps/26/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma2.jpg;type=image/jpg" %API_URL%/apps/26/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma3.jpg;type=image/jpg" %API_URL%/apps/26/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma4.jpg;type=image/jpg" %API_URL%/apps/26/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma5.jpg;type=image/jpg" %API_URL%/apps/26/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\uptime-kuma\uptime-kuma6.jpg;type=image/jpg" %API_URL%/apps/26/screenshots
echo.

echo.
echo App27
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app27-netbox.json
echo.
echo App27 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\netbox.svg;type=image/svg+xml" %API_URL%/apps/27/logo
echo.
echo App27 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\netbox\netbox1.png;type=image/png" %API_URL%/apps/27/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\netbox\netbox2.png;type=image/png" %API_URL%/apps/27/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\netbox\netbox3.png;type=image/png" %API_URL%/apps/27/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\netbox\netbox4.png;type=image/png" %API_URL%/apps/27/screenshots
echo.

echo.
echo App28
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app28-zabbix.json
echo.
echo App28 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\zabbix.png;type=image/png" %API_URL%/apps/28/logo
echo.
echo App28 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\zabbix\zabbix1.png;type=image/png" %API_URL%/apps/28/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\zabbix\zabbix2.png;type=image/png" %API_URL%/apps/28/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\zabbix\zabbix3.png;type=image/png" %API_URL%/apps/28/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\zabbix\zabbix4.png;type=image/png" %API_URL%/apps/28/screenshots
echo.

echo.
echo ---------------------
echo Activate apps
curl -X PATCH %API_URL%/apps/state/1 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/2 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/3 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/4 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/5 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/6 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/7 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/8 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/9 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/10 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/11 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/12 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/13 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/14 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/15 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/16 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/17 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/18 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/19 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/20 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/21 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/22 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/23 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/24 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/25 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/26 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/27 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/28 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/29 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
curl -X PATCH %API_URL%/apps/state/30 --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\activations\active.json
echo.

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
curl -X POST %API_URL%/subscriptions --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\subscriptions\sub1.json
echo
echo Create app2 subscription to Domain One
curl -X POST %API_URL%/subscriptions --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\subscriptions\sub2.json
echo
echo Create app3 subscription to Domain One
curl -X POST %API_URL%/subscriptions --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\subscriptions\sub3.json
echo
echo Create app3 subscription to Domain Two
curl -X POST %API_URL%/subscriptions --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\subscriptions\sub4.json
echo

echo.
echo ---------------------
echo Create english language content
curl -X POST %API_URL%/i18n/en?enabled=true --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\i18n\en.json
echo
echo Create french language content
curl -X POST %API_URL%/i18n/fr?enabled=true --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\i18n\fr.json
echo
echo Create polish language content
curl -X POST %API_URL%/i18n/pl?enabled=true --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\i18n\pl.json
echo
echo Create german language content
curl -X POST %API_URL%/i18n/de?enabled=true --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\i18n\de.json
echo

echo.
echo ---------------------
echo Insert form type contact
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\contact.json
echo
echo Insert form type issue
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\issue_report.json
echo
echo Insert form type feature request
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\feature_request.json
echo
echo Insert form type access request
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\access_request.json
echo
echo Insert form type domain request
curl -X PUT %API_URL%/mail/type --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\form_types\domain_request.json
echo