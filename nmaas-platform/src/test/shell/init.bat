@echo off
setlocal enableDelayedExpansion

set API_URL=http://localhost:9000/portal/api
echo %API_URL%

set LF=^




for /f "delims=" %%f in ('curl -X POST %API_URL%/auth/basic/login --header "Content-Type: application/json" --header "Accept: application/json" -d @data\login.json') do (
REM set "LOGIN="
	if defined LOGIN set "LOGIN=!LOGIN!!LF!"
	set "LOGIN=!LOGIN!%%f"
)

rem echo Output: !LOGIN!
REM ref: http://stackoverflow.com/questions/36374496/parse-simple-json-string-in-batch
set LOGIN=!LOGIN:"=!
rem stage 1 - !LOGIN!
set "LOGIN=!LOGIN:~2,-2!"
rem stage 2 - !LOGIN!
set "LOGIN=!LOGIN: : ==!"
rem stage 3 - !LOGIN!


FOR /F "delims=," %%a in ("!LOGIN!") do (
rem  echo Found: %%a
  set "%%a"
)

echo Token: 
echo ----------------------
echo %token%
echo ----------------------
echo Ping
curl -X GET %API_URL%/auth/basic/ping --header "Authorization: Bearer %token%"

echo.
echo App1
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app1-librenms.json

echo.
echo App1 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\librenms.png;type=image/png" %API_URL%/apps/1/logo

echo.
echo App1 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\librenms\librenms.png;type=image/png" %API_URL%/apps/1/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\librenms\librenms-2.png;type=image/png" %API_URL%/apps/1/screenshots


echo.
echo App2
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app2-oxidized.json

echo.
echo App2 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\oxidized.svg;type=image/svg" %API_URL%/apps/2/logo

echo.
echo App2 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\oxidized\oxidized-web.png;type=image/png" %API_URL%/apps/2/screenshots


echo.
echo App3
curl -X POST %API_URL%/apps --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\app3-pmacct.json

echo.
echo App3 logo
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\logo\pmacct.svg;type=image/svg" %API_URL%/apps/3/logo

echo.
echo App3 screenshots
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\pmacct\pmacct-2.jpg;type=image/jpg" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\pmacct\pmacct-month.png;type=image/png" %API_URL%/apps/3/screenshots
echo.
curl -X POST --header "Authorization: Bearer %token%" -F "file=@data\apps\images\screenshots\pmacct\pmacct-pnrg-peerings-big.jpg;type=image/png"  %API_URL%/apps/3/screenshots


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
echo Get first app
curl -X GET %API_URL%/apps/1 --header "Authorization: Bearer %token%"


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
curl -X POST %API_URL%/apps/2/rate/my/2 --header "Authorization: Bearer %token%" 

echo.
echo Rate App3
curl -X POST %API_URL%/apps/3/rate/my/5 --header "Authorization: Bearer %token%" 


echo.
echo ---------------------
echo Tags:
curl -X GET %API_URL%/tags --header "Authorization: Bearer %token%" --header "Accept: application/json"

echo.
echo ---------------------
echo By tag:
curl -X GET %API_URL%/tags/management --header "Authorization: Bearer %token%" --header "Accept: application/json"

echo.
echo ---------------------

