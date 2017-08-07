# NMaaS Portal (GUI)

##### Web-based GUI for browsing, subscription and deployment of services and tools offered by NMaaS.

--

#### Technologies
---
 * Angular 2 (2.3.1)
 * Bootstrap 3
 * JWT authentication
 * Spring 4 with REST API for GUI backend

#### Prerequisites
---
 + Install npm (from NodeJS 6.11.0+) ([https://nodejs.org](https://nodejs.org))
 + Install angular-cli (version 1.1.3+) ([http://cli.angular.io](http://cli.angular.io))
   - npm install -g @angular/cli@latest
 + Install git ([https://git-scm.com/](https://git-scm.com/))

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
  