# nmaas Platform

### Technologies

* Java 21
* Spring Boot 3.5
* Gitlab4j
* Kubernetes API client from fabric8
* Flyway
* freemarker

### Running nmaas Platform on local machine

#### Prerequisites
Install Java 21 jdk

#### Running the software
In order to build and run the nmaas Platform software locally execute *./gradlew bootRun* in project root directory.
By default, the REST API will be exposed at *http://localhost:9000/api*.


### Running nmaas Platform on dedicated server machine

To run the nmaas Platform on a dedicated machine, perform the following steps:
+ Build the nmaas Platform with *./gradlew clean build* command project root directory.
+ Retrieve the output executable *nmaas-platform-x.x.x.jar* file from *build/libs* directory.
+ Run the nmaas Platform with *java -jar nmaas-platform-x.x.x.jar* and optional arguments:
    + *-Dlogging.config* specifying the name of logger configuration file (located in the same directory as the jar file) to be loaded instead of the built-in one. Please be advised that you have to add that parameter before the *-jar* parameter.
    + *--spring.config.name* specifying the name of the properties file (located in the same directory as the jar file) to be loaded instead of the built-in one.

### OpenAPI documentation of the nmaas Platform REST API

nmaas Platform by default exposes two endpoints documenting the REST API:
+ */api-docs/spec* - Open API specification of the API
+ */api-docs/ui.html* - Swagger UI

These endpoints can be disabled in properties file.


### Populating nmaas Platform database with initial data

To initialize the nmaas Platform database with a default set of data run *src/test/shell/init.sh* script.
Script will load the following data:
+ set of content translation data (from *src/test/shell/data/i18n*)
+ set of default email templates (from *src/test/shell/data/mails*)
+ set of default contact form templates (from *src/test/shell/data/contact_forms*)
+ set of test nmaas user domains (from *src/test/shell/data/domains*)
+ set of nmaas-compatible application definitions with test subscriptions and comments (from *src/test/shell/data/apps*)


### Complete deployment environment setup for Kubernetes

A dedicated Helm chart has been developed to ease the nmaas installation inside a Kubernetes cluster.
This way both the supported network management applications and the nmaas core components can be installed within a single K8s cluster.
For more information please contact the [nmaas Team](mailto:nmaas-team@lists.geant.org).


### Generate OpenAPI spec yaml file based on the current implementation of REST API endpoints

+ Execute: *./gradlew generateOpenApiYaml* in project folder.
+ Get yaml file  *build/openapi/openapi.yaml*