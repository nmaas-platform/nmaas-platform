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

echo.
echo Configuration
curl -X POST %API_URL%/configuration --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\configuration.json
echo.
echo App1
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app1-librenms.json
echo.
echo App1 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\librenms.png;type=image/png" %API_URL%/apps/1/logo
echo.
echo App1 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\librenms\librenms1.png;type=image/png" %API_URL%/apps/1/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\librenms\librenms2.png;type=image/png" %API_URL%/apps/1/screenshots

echo.
echo App2
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app2-oxidized.json
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
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app3-nav.json
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
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app4-opennti.json
echo.
echo App4 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\juniper.svg;type=image/svg+xml" %API_URL%/apps/4/logo
echo.
echo App4 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\opennti\opennti1.png;type=image/jpg" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\opennti\opennti2.png;type=image/png" %API_URL%/apps/4/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\opennti\opennti3.png;type=image/png" %API_URL%/apps/4/screenshots

rem echo.
rem echo App5
rem curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app5-pmacct.json
rem echo.
rem echo App5 logo
rem curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\pmacct.svg;type=image/svg+xml" %API_URL%/apps/5/logo
rem echo.
rem echo App5 screenshots
rem curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\pmacct\pmacct1.jpg;type=image/jpg" %API_URL%/apps/3/screenshots
rem echo.
rem curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\pmacct\pmacct2.png;type=image/png" %API_URL%/apps/3/screenshots
rem echo.
rem curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\pmacct\pmacct3.jpg;type=image/png"  %API_URL%/apps/3/screenshots

echo.
echo ---------------------
echo Get all apps
curl -X GET %API_URL%/apps --header "Authorization: Bearer %token%"

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
echo Get second app
curl -X GET %API_URL%/apps/2/complete --header "Authorization: Bearer %token%"

echo.
echo ---------------------
echo Get comments for first app
curl -X GET %API_URL%/apps/1/comments --header "Authorization: Bearer %token%"

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

rem echo.
rem echo Rate App5
rem curl -X POST %API_URL%/apps/5/rate/my/5 --header "Authorization: Bearer %token%"

echo.
echo ---------------------
echo Tags:
curl -X GET %API_URL%/tags --header "Authorization: Bearer %token%" --header "Accept: application/json"

echo.
echo ---------------------
echo By tag:
curl -X GET %API_URL%/tags/management --header "Authorization: Bearer %token%" --header "Accept: application/json"

rem echo.
rem echo ---------------------
rem echo Create app2 instance
rem curl -X POST %API_URL%/domains/2/apps/instances --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\instances\instance1.json
rem echo.
rem echo ---------------------
rem echo Get app instance 1 state
rem curl -X GET %API_URL%/domains/2/apps/instances/1/state --header "Authorization: Bearer %token%" --header "Accept: application/json"
rem echo.
rem echo ---------------------
rem echo Get app instance 1
rem curl -X GET %API_URL%/domains/2/apps/instances/1 --header "Authorization: Bearer %token%" --header "Accept: application/json"
rem echo.
rem echo ---------------------
rem echo Send configuration to instance 1
rem curl -X POST %API_URL%/domains/2/apps/instances/1/configure --header "Authorization: Bearer %token%" --header "Content-Type: application/json" -d @data\apps\instances\instance1-config.json
rem echo.
rem echo ---------------------
rem echo Get app instance 1
rem curl -X GET %API_URL%/domains/2/apps/instances/1 --header "Authorization: Bearer %token%" --header "Accept: application/json"
rem 
echo.
echo ---------------------
echo Get app 1 logo without token
curl -X GET %API_URL%/apps/1/logo
