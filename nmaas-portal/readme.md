# NMaaS Portal (GUI)

##### Web-based GUI for browsing, subscription and deployment of services and tools offered by NMaaS.

--

#### Technologies
---
 * Angular 5 (5.2.x)
 * Bootstrap 3
 * JWT authentication
 * Spring 5 with REST API for GUI backend

#### Prerequisites
---
 + Install npm (>=5.0) and NodeJS (>=8.0)
 + Install git
 + (run) Install http server 
   - npm install -g angular-http-server
 + For testing purpose install all required libraries
 *https://docs.browserless.io/blog/2018/04/25/chrome-linux.html*

#### Build and run
---
##### Server environment
---
  + Build the Portal with *gradlew clean build* in reactor directory.
  + The output archive *nmaas-portal-x.x.x.zip* file is created in *nmaas-portal/build/distributions* directory.
  + Run the http server in *nmaas-portal/build/app*
    - nohup angular-http-server -p 9009 -s --cors > nmaas-portal.log 2> Error.err < /dev/null &

##### Local environment
---
  + Go to *nmaas-portal* directory in terminal or command line
  + First run requires typing *npm install* command in order to install all of missing dependencies
  + Type command *npm start*
    - After successful compilation go to *http://localhost:4200* in your browser (do not close terminal or command line)

#### Tests
---
+ Run *gradlew run testCoverage* in nmaas reactor directory
+ Results of tests are displayed on screen. More information about code quality (including code coverage, test status) are avaliable in *nmaas-portal/coverage/index.html*

#### Defaults
---
  + URL:
    - WWW: http://(HOSTNAME):9009
    - API: http://(HOSTNAME):9001/api
  + Access:
    - username: admin
    - password: admin