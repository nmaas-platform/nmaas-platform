# nmaas Platform

### Technologies

* Java 25
* Spring Boot 4
* Gitlab4j
* Kubernetes API client from fabric8
* Flyway
* freemarker

### Running nmaas Platform on local machine

#### Prerequisites
Install Java 25 jdk

#### Running the software
To build and run the nmaas Platform software locally, execute *./gradlew bootRun* in the project root directory.
By default, the REST API will be exposed at *http://localhost:9000/api/v1*.


### Running nmaas Platform on a dedicated server machine

To run the nmaas Platform on a dedicated machine, perform the following steps:
+ Build the nmaas Platform with *./gradlew clean build* command project root directory.
+ Retrieve the output executable *nmaas-platform-x.x.x.jar* file from *the build / libs* directory.
+ Run the nmaas Platform with *java -jar nmaas-platform-x.x.x.jar* and optional arguments:
    + *-Dlogging.config* specifying the name of the logger configuration file (located in the same directory as the jar file) to be loaded instead of the built-in one. Please be advised that you have to add that parameter before the *-jar* parameter.
    + *--spring.config.name* specifying the name of the properties file (located in the same directory as the jar file) to be loaded instead of the built-in one.

### OpenAPI documentation of the nmaas Platform REST API

nmaas Platform by default exposes two endpoints documenting the REST API:
+ */api-docs/spec* - Open API specification of the API
+ */api-docs/ui.html* - Swagger UI

These endpoints can be disabled in the properties file.


### Populating nmaas Platform database with initial data

To initialize the nmaas Platform database with a default set of data, run *src/test/shell/init.sh* script.
Script will load the following data:
+ set of content translation data (from *src/test/shell/data/i18n*)
+ set of default email templates (from *src/test/shell/data/mails*)
+ set of default contact form templates (from *src/test/shell/data/contact_forms*)
+ set of nmaas-compatible application definitions (from *src/test/shell/data/apps*)

Please note that translations, email templates, and contact forms are loaded from the *src/test/shell/data* directory using relative paths, while application definitions are loaded from the */nmaas/init/data/apps* absolute path by *src/test/shell/scripts/app.sh* (a path typically mounted in the container runtime).


### Complete deployment environment setup for Kubernetes

A dedicated Helm chart has been developed to ease the nmaas installation inside a Kubernetes cluster.
This way both the supported network management applications and the nmaas core components can be installed within a single K8s cluster.
For more information please contact the [nmaas Team](mailto:nmaas-team@lists.geant.org).


### Generate OpenAPI spec JSON file based on the current implementation of REST API endpoints

+ Execute: *./gradlew generateOpenApiJson* in the project folder.
+ Get JSON file  *build/openapi/openapi.json*
