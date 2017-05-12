# NMaaS Portal (GUI)

Technologies:
 * Angular 2 (2.3.1)
 * Bootstrap 3
 * JWT authentication
 * Spring 4 with REST API for GUI backend

Prerequisities:
---

 + install npm (from NodeJS 6.9.5+)
   - [https://nodejs.org](https://nodejs.org)
 + install angular-cli (1.0.0-beta.28.3)
   - [http://cli.angular.io](http://cli.angular.io)
   - npm install -g angular-cli
 + install git
   - [https://git-scm.com/](https://git-scm.com/)

Known issues:
---
 + On Windows: 
   There is a bug in Gradle <=3.3 causing failure even if everything is build successfully [Gradle Issue 882](https://github.com/gradle/gradle/issues/882)
   The workaround is to execute gradle command with '--console plain' param 
   e.g. gradle build --console plain
  