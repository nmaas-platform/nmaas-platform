@echo off
setlocal enableDelayedExpansion
@echo off
setlocal enableDelayedExpansion

set PORTAL_API_URL=http://localhost:9000/api
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

set API_URL=http://localhost:9000/api
echo %API_URL%

echo.
echo Adding default Docker Hosts
curl -X POST %API_URL%/management/dockerhosts --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\inventory\dockerhosts\docker-host-1.json
curl -X POST %API_URL%/management/dockerhosts --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\inventory\dockerhosts\docker-host-2.json
curl -X POST %API_URL%/management/dockerhosts --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\inventory\dockerhosts\docker-host-3.json
echo
curl -X GET %API_URL%/management/dockerhosts --header "Authorization: Bearer %token%"

echo.
echo Adding default Kubernetes
curl -X POST %API_URL%/management/kubernetes --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\inventory\kubernetes\kubernetes-1.json
echo
curl -X GET %API_URL%/management/kubernetes --header "Authorization: Bearer %token%"

echo.
echo Adding default Docker Host attachment points
curl -X POST %API_URL%/management/network/dockerhosts --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\inventory\dockerhosts\docker-host-1-attach-point.json
echo
curl -X GET %API_URL%/management/network/dockerhosts --header "Authorization: Bearer %token%"

echo.
echo Adding default network attachment point to default domain testdom1
curl -X POST %API_URL%/management/domains/testdom1/network --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\inventory\domains\test-domain-1-network-attach-point.json
echo
curl -X GET %API_URL%/management/domains/testdom1/network --header "Authorization: Bearer %token%"
echo.