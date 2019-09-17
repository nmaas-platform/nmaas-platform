# NMaaS Platform (backend)

##### NMaaS Platform implements mechanisms for on-demand network management applications deployment in the cloud environment and connectivity setup between the managed equipment and the running application.

  NM applications are containerized and deployed using [Helm charts](https://helm.sh/).
  In order to set up a secure connection between the deployed NM application and the monitored/managed customer equipment a set of [Ansible](https://www.ansible.com/) playbooks may be executed (if network configuration automation is desired).
  Platform exposes a REST API consumed by the NMaaS Portal GUI.

### Technologies
---
 * Java 8
 * Spring 5
 * Spring Boot (2.1.5)
 * spotify:docker-client (8.8.0)
 * freemarker (2.3.28)
 * Google Guava (23.0)
 * Swagger (2.8.0)
 * Log4j2 (2.11.1)
 * Lombok (1.18.2)
 * Gitlab4j (4.9.18)
 * Flyway (5.2.0)

### Prerequisites
---
  + Install java 8 jdk ([http://www.oracle.com](http://www.oracle.com/technetwork/java/javase/downloads))

### Running Platform locally
---
  In order to build and run the Platform software locally execute *./gradlew bootRun* in this directory.

### Running Platform on dedicated machine
---
  In order to run Platform on dedicated machine perform the following steps:
  + Build the Platform with *./gradlew clean build* in this directory.
  + The output executable *nmaas-platform-x.x.x.jar* file is created in *nmaas-platform/build/libs* directory.
  + Run the Platform with *java -jar nmaas-platform-x.x.x.jar*.
  + Optionally run the Platform with additional argument *--Dlog4j.configurationFile* specifying the name of logger (Log4j2) configuration file (located in the same directory as the jar file) to be loaded instead of the built in one. Please be advised that you have to add that parameter before the *-jar* parameter.
  + It is advised to run the Platform with additional argument *--spring.config.name* specifying the name of the properties file (located in the same directory as the jar file) to be loaded instead of the built in one.

#### Populating Platform database with initial data
---
  To add pre-defined set of applications to the Platform run *nmaas-platform/src/test/shell/init.sh* script that will load NMaaS-compatible application definitions included in the *nmaas-platform/src/test/shell/data/apps* directory.
  Initial deployment environment data/configuration may be applied by running *nmaas-platform/src/test/shell/populate-inventory.sh* script that will load customer network, Kubernetes cluster and GitLab definitions included in the *nmaas-platform/src/test/shell/data/inventory* directory.

#### Complete deployment environment setup for Kubernetes
---
  A dedicated Helm chart has been developed to ease the NMaaS installation within the Kubernetes cluster. This way both the supported network management applications and the NMaaS core components can be installed within a Kubernetes cluster. For more information please contact the [NMaaS Team](mailto:nmaas-team@lists.geant.org).

#### Automated DCN setup with Ansible
---
  Platform supports execution of Ansible playbooks to setup a dedicated DCN network. Current solution is tailored to configure L3 VPNs in Juniper-based networks.
  In order to enable automatic DCN configuration:
  + Deploy a dedicated machine running Docker for executing Ansible playbooks inside a dedicated container.
  + Pull Docker images on that machine that allow for Ansible playbook execution.

### Building and uploading Platform Docker image
---
  In order to build a Platform Docker image first alter the *build_and_publish.sh* with custom REPOSITORY, PACKAGE and TAG values and execute *build_and_publish.sh* to automatically build and publish *nmaas-platform* image to selected Docker repository.
