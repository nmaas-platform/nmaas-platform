# NMaaS Platform (back-end)

#### NMaaS Platform implements mechanisms for on-demand network management applications deployment in the cloud environment and connectivity setup between the managed equipment and the running application.

NM applications are containerized and deployed using [Helm charts](https://helm.sh/).
Platform exposes a REST API consumed by the NMaaS Portal GUI.

### Technologies
---
 * Java 11
 * Spring 5
 * Spring Boot (2.6.3)
 * freemarker (2.3.31)
 * Gitlab4j (4.17.0)
 * Flyway (5.2.0)
 * Kubernetes client from fabric8 (4.10.2)

### Prerequisites
---
  Install Java 11 jdk

### Running NMaaS Platform locally
---
  In order to build and run the NMaaS Platform software locally execute *./gradlew bootRun* in this directory.

### Running NMaaS Platform on dedicated machine
---
  In order to run the NMaaS Platform on dedicated machine perform the following steps:
  + Build the NMaaS Platform with *./gradlew clean build* command project root directory.
  + Retrieve the output executable *nmaas-platform-x.x.x.jar* file from *build/libs* directory.
  + Run the NMaaS Platform with *java -jar nmaas-platform-x.x.x.jar* and optional arguments:
    + *--Dlog4j.configurationFile* specifying the name of logger (Log4j2) configuration file (located in the same directory as the jar file) to be loaded instead of the built-in one. Please be advised that you have to add that parameter before the *-jar* parameter.
    + *--spring.config.name* specifying the name of the properties file (located in the same directory as the jar file) to be loaded instead of the built-in one.

#### Populating NMaaS Platform database with initial data
---
  To initialize the NMaaS Platform database with a default set of data run *src/test/shell/init.sh* script.
  Script will load the following data:
  + set of content translation data (from *src/test/shell/data/i18n*)
  + set of default email templates (from *src/test/shell/data/mails*)
  + set of default contact form templates (from *src/test/shell/data/form_types*)
  + set of test NMaaS user domains (from *src/test/shell/data/domains*)
  + set of NMaaS-compatible application definitions with test subscriptions and comments (from *src/test/shell/data/apps*)

#### Complete deployment environment setup for Kubernetes
---
A dedicated Helm chart has been developed to ease the NMaaS installation inside a Kubernetes cluster.
This way both the supported network management applications and the NMaaS core components can be installed within a single K8s cluster.
For more information please contact the [NMaaS Team](mailto:nmaas-team@lists.geant.org).

### Building and uploading NMaaS Platform Docker image
---
In order to build the NMaaS Platform Docker image first alter the *build_and_publish.sh* with custom REPOSITORY, PACKAGE and TAG values and execute *build_and_publish.sh* to automatically build and publish *nmaas-platform* image to selected Docker repository.
