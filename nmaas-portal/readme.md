# NMaaS Portal (GUI)

### Web-based GUI for browsing, subscription and deployment of network management applications offered by the NMaaS system

### Issues
---
As of Angular 9, there is an issue with ```ivy``` and ```ngcc```, regarding ```angular-formio```, however everything seems to be working fine.  
See [Github Issue](https://github.com/formio/angular-formio/issues/485)

### Technologies
---
see package.json

### Prerequisites
---
 + Install node and npm
 + Install git
 + (running on server) Install http server with *npm install -g angular-http-server*
 + (running tests) Install all required libraries as listed in *https://docs.browserless.io/blog/2018/04/25/chrome-linux.html*

### Running NMaaS Portal locally
---
Go to *nmaas-portal* directory in terminal or command line
Run command *npm start*
After successful compilation go to *http://localhost:4200* in your browser (do not close terminal or command line)
Note: First run requires entering *npm install* command in order to install all of missing dependencies

### Running NMaaS Portal on dedicated machine
---
  In order to run NMaaS Portal on dedicated machine perform the following steps:
  + Build the NMaaS Portal by running *gradlew clean build* in the reactor directory.
    - In order to build for production environment use additional option *-Pprod*
  + The output archive *nmaas-portal-x.x.x.zip* file is created in *nmaas-portal/build/distributions* directory.
  + Run the http server in *nmaas-portal/build/app*
    - *nohup angular-http-server -p 9009 -s --cors > nmaas-portal.log 2> Error.err < /dev/null &*

### Lunching tests
---
Run *./gradlew run testCoverage* in this directory.
Results of executed tests are displayed on screen. 
More information about code quality (including code coverage, test status) are available in *nmaas-portal/coverage/index.html*

### Default settings
---
NMaaS Portal and Platform URLs:
+ Portal: *http://<HOSTNAME>:9009*
+ Platform API: *http://<HOSTNAME>:9001/api*

Admin user:
+ username: *admin*
+ password: *admin*
    
### Building and uploading NMaaS Portal Docker image
---
In order to build the NMaaS Portal Docker image first alter the *build_and_publish.sh* script with custom REPOSITORY, PACKAGE and TAG values and execute *build_and_publish.sh* to automatically build and publish *nmaas-portal* image to selected Docker repository.
