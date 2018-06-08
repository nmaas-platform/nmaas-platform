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
    - API: http://<HOSTNAME>:9001/portal/api
  + Access:
    - username: admin
    - password: admin

#### Known issues
---
 + On Windows: 
   There is a bug in Gradle <=3.3 causing failure even if everything is build successfully [Gradle Issue 882](https://github.com/gradle/gradle/issues/882)
   The workaround is to execute gradle command with '--console plain' param 
   e.g. gradle build --console plain
  