# NMaaS Portal (GUI)

##### Web-based GUI for browsing, subscription and deployment of services and tools offered by NMaaS.

--

#### Technologies
---
 * Angular 7 (7.2.x)
 * Bootstrap 3
 * JWT authentication

#### Prerequisites
---
 + Install npm (>=6.0) and NodeJS (>=8.0)
 + Install git
 + (running on server) Install http server with *npm install -g angular-http-server*
 + (running tests) Install all required libraries as listed in *https://docs.browserless.io/blog/2018/04/25/chrome-linux.html*

#### Build and run
---
##### Deploying docker image
---
  + alter build_and_publish.sh with custom ARTIFACTORY, PACKAGE and TAG variables
  + run build_and_publish.sh to automatically build and publish nmaas-portal image to selected artifactory

##### Local environment
---
  + Go to *nmaas-portal* directory in terminal or command line
  + First run requires typing *npm install* command in order to install all of missing dependencies
  + Run command *npm start*
  + After successful compilation go to *http://localhost:4200* in your browser (do not close terminal or command line)

##### Server environment
---
  + Build the Portal with *gradlew clean build* in the reactor directory.
    - In order to build for production environment use additional option *-Pprod*
  + The output archive *nmaas-portal-x.x.x.zip* file is created in *nmaas-portal/build/distributions* directory.
  + Run the http server in *nmaas-portal/build/app*
    - *nohup angular-http-server -p 9009 -s --cors > nmaas-portal.log 2> Error.err < /dev/null &*

#### Tests
---
  + Run *./gradlew run testCoverage* in this directory.
  + Results of tests are displayed on screen. 
  + More information about code quality (including code coverage, test status) are available in *nmaas-portal/coverage/index.html*

#### Defaults
---
  + Portal and Platform URLs:
    - Portal: *http://<HOSTNAME>:9009*
    - Platform API: *http://<HOSTNAME>:9001/api*
  + Admin user:
    - username: *admin*
    - password: *admin*
