@echo off
setlocal enableDelayedExpansion

set API_URL=http://localhost:9000/portal/api
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
echo ---------------------
echo Set ADMIN role TOOL_MANAGER
curl -X POST %API_URL%/domains/1/users/1/roles --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\users\superadmin-role-toolmanager.json

echo.
echo ---------------------
echo Get ADMIN roles
curl -X GET %API_URL%/users/1/roles --header "Authorization: Bearer %token%" --header "Accept: application/json"

echo.
echo ---------------------
echo Create Domain1
curl -X POST %API_URL%/domains --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\domains\domain1.json
echo.

echo.
echo ---------------------
echo Create Domain 1 admin
curl -X POST %API_URL%/users --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\users\domain1-admin.json
echo.
echo ---------------------
echo Add domain1admin role DOMAIN_ADMIN
curl -X POST %API_URL%/domains/2/users/2/roles --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\users\domain1-admin-role1.json


echo.
echo ---------------------
echo Get all users
curl -X GET %API_URL%/users --header "Authorization: Bearer %token%" --header "Accept: application/json"

echo.
echo ---------------------
echo Create app2 instance
curl -X POST %API_URL%/domains/2/apps/instances --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\apps\instances\instance1.json
echo.
echo ---------------------
echo Get app instance 1 state
curl -X GET %API_URL%/domains/2/apps/instances/1/state --header "Authorization: Bearer %token%" --header "Accept: application/json"
echo.
echo ---------------------
echo Get app instance 1
curl -X GET %API_URL%/domains/2/apps/instances/1 --header "Authorization: Bearer %token%" --header "Accept: application/json"
echo.
echo ---------------------
echo Send configuration to instance 1
curl -X POST %API_URL%/domains/2/apps/instances/1/configure --header "Authorization: Bearer %token%" --header "Content-Type: application/json" -d @data\apps\instances\instance1-config.json
echo.
echo ---------------------
echo Get app instance 1
curl -X GET %API_URL%/domains/2/apps/instances/1 --header "Authorization: Bearer %token%" --header "Accept: application/json"


