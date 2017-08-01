@echo off
setlocal enableDelayedExpansion
@echo off
setlocal enableDelayedExpansion

set PORTAL_API_URL=http://localhost:9000/portal/api
echo %PORTAL_API_URL%

set LF=^




for /f "delims=" %%f in ('curl -sX POST %PORTAL_API_URL%/auth/basic/login --header "Content-Type: application/json" --header "Accept: application/json" -d @data\login.json') do (
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
curl -X GET %PORTAL_API_URL%/auth/basic/ping --header "Authorization: Bearer %token%"

set API_URL=http://localhost:9000/platform/api
echo %API_URL%

echo.
echo Adding default configuration templates for app 1 and app 2
curl -X POST %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configurations\app1-template1.json
curl -X POST %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configurations\app2-template1.json
curl -X POST %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\configurations\app2-template2.json
echo
curl -X GET %API_URL%/management/configurations/templates --header "Authorization: Bearer %token%"

echo.
echo Adding default docker compose templates for app 1 and app 2
curl -X POST %API_URL%/management/apps/1/dockercompose/template --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\dockercompose\app1-template1.json
curl -X POST %API_URL%/management/apps/2/dockercompose/template --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\templates\dockercompose\app2-template1.json
echo
curl -X GET %API_URL%/management/apps/1/dockercompose/template --header "Authorization: Bearer %token%"
curl -X GET %API_URL%/management/apps/2/dockercompose/template --header "Authorization: Bearer %token%"