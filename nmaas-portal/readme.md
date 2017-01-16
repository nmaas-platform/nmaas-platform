
Prerequisities:
---

 + install npm
   - [https://nodejs.org](https://nodejs.org)
 + install angular-cli 
   - [http://cli.angular.io](http://cli.angular.io)
   - npm install -g angular-cli
   

Known issues:
---
 + On Windows: 
   There is a bug in Gradle <=3.3 causing failure even if everything is build successfully [Gradle Issue 882](https://github.com/gradle/gradle/issues/882)
   The workaround is to execute gradle command with '--console plain' param 
   e.g. gradle build --console plain
  