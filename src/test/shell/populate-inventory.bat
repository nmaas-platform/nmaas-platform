@echo off
setlocal enableDelayedExpansion
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
echo Adding default Kubernetes
curl -X POST %API_URL%/management/kubernetes --header "Authorization: Bearer %token%" --header "Content-Type: application/json" --header "Accept: application/json" -d @data\inventory\kubernetes\kubernetes-1.json
echo
curl -X GET %API_URL%/management/kubernetes --header "Authorization: Bearer %token%"