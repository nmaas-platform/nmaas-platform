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
echo Adding default configuration templates for app 1, app 2 and app 4
curl -X POST %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configurations\app1-template1.json
curl -X POST %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configurations\app2-template1.json
curl -X POST %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configurations\app2-template2.json
curl -X POST %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configurations\app4-template1.json
curl -X POST %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configurations\app4-template2.json
echo
curl -X GET %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%"

echo.
echo Adding default docker compose templates for app 1, app 2, app3 and app4
curl -X POST %API_URL%/management/apps/1/dockercompose/template --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\dockercompose\app1-template1.json
curl -X POST %API_URL%/management/apps/2/dockercompose/template --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\dockercompose\app2-template1.json
curl -X POST %API_URL%/management/apps/3/dockercompose/template --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\dockercompose\app3-template1.json
curl -X POST %API_URL%/management/apps/4/dockercompose/template --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\dockercompose\app4-template1.json
echo
curl -X GET %API_URL%/management/apps/1/dockercompose/template --header "Authorization: Bearer %token%"
curl -X GET %API_URL%/management/apps/2/dockercompose/template --header "Authorization: Bearer %token%"
curl -X GET %API_URL%/management/apps/3/dockercompose/template --header "Authorization: Bearer %token%"
curl -X GET %API_URL%/management/apps/4/dockercompose/template --header "Authorization: Bearer %token%"

echo.
echo Adding default kubernetes template for app 2
curl -X POST %API_URL%/management/apps/2/kubernetes/template --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\kubernetes\app2-template1.json
echo
curl -X GET %API_URL%/management/apps/2/kubernetes/template --header "Authorization: Bearer %token%"
