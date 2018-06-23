# NMaaS Portal (GUI)

##### Web-based GUI for browsing, subscription and deployment of services and tools offered by NMaaS.

--

#### Technologies
---
 * Angular 5 (5.2.x)
 * Bootstrap 3
 * JWT authentication
 * Spring 4 with REST API for GUI backend

#### Prerequisites
---
 + Install npm (from NodeJS 6.11.0+) ([https://nodejs.org](https://nodejs.org))
 + Install git ([https://git-scm.com/](https://git-scm.com/))
 + (run) Install http server 
   - npm install -g angular-http-server

#### Build and run
---
  + Build the Portal with *gradle clean build* in reactor directory.
  + The output archive *nmaas-portal-0.3.0.zip* file is created in *nmaas-portal/build/distributions* directory.
  + Run the http server in *nmaas-portal/build/app*
    - nohup angular-http-server -p 9009 -s --cors > nmaas-portal.log 2> Error.err < /dev/null &

#### Defaults
---
  + URL:
    - WWW: http://<HOSTNAME>:9009
    - API: http://<HOSTNAME>:9001/api
  + Access:
    - username: admin
    - password: admin

#### Known issues
---
 + On Windows: 
   There is a bug in Gradle <=3.3 causing failure even if everything is build successfully [Gradle Issue 882](https://github.com/gradle/gradle/issues/882)
   The workaround is to execute gradle command with '--console plain' param 
   e.g. gradle build --console plain
  
#### Federated login configuration
---

  + The main concept is trusting an external authorization source on the basis of a data exchange signed by a shared secret key.
  
  + Flows are as follows :
    - user goes to nmaas portal
    - portal redirects user to SAML proxy
    - SAML proxy does what is needed to authenticate user
    - SAML proxy redirects user back to nmaas portal with userID (basically a signed, time limited username)
    - nmaas portal sends userID to nmaas federated login api endpoint
    - api validates userID and create a bearer token using the given username
  
  + This system allows to delegate current logged-in user name management to an external application with very loose coupling (http redirects and shared secret key) and still keep the user domains and privileges management in the nmaas application.

  + Besides For this to work you need to do add the "sso" entry in the portal json config :
    - name : the text that will be display on the login button in the login page if you also allow basic auth at the same time
    - allowBasic : if set to true the user will be able to choose between local and sso auth in the login page, if set to false the user will be redirected to the sso login url immediately upon login page access
    - loginUrl : url the user will be redirected to for login, at the end of the authentication process the user must be sent back to the url given through the "return" query parameter with a userID (see below)
    - logoutUrl : url the user will be redirected to for logout, must do whatever is needed to log the user out of the sso    
    - userID : transmitted through the "ssoUserId" query parameter, created by :
      - building the timestamped username by concatenating the base 64 encoded username, a pipe ("|") and the current unix timestamp (long)
      - generating a signature of the timestamped username by computing a hmac sha256 signature of it using the shared key
      - concatenating the timestamped username, a pipe ("|") and the hex encoded signature
      - this gives a string like: YWRtaW4=|1521813856|8aa9e4382dfe57f21b9bf420f968f32abbd3d978d9e0adc5ed548b6e04b10425

```
    "sso": {
        "name": "SSO service",
        "loginUrl": "http://localhost/sp/login",
        "logoutUrl": "http://localhost/sp/logout",
        "allowsBasic": true
    }
```
