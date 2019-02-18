# NMaaS Platform (backend)

##### NMaaS Platform implements mechanisms for on-demand network management applications deployment in the cloud environment and connectivity setup between the managed equipment and the running application.

###### NM applications are containerized and can be deployed using [Helm charts](https://helm.sh/).
###### In order to set up a secure connection between the deployed NM application and the monitored/managed customer equipment a set of [Ansible](https://www.ansible.com/) playbooks may be executed (if network configuration automation is desired).

###### Platform exposes a REST API consumed by the NMaaS Portal GUI.
#
### Technologies
---
 * Java 8
 * Spring 5
 * Spring Boot (2.0.6)
 * spotify:docker-client (8.8.0)
 * freemarker (2.3.20)
 * Google Guava (23.0)
 * Swagger (2.8.0)
 * Log4j2 (2.11.1)
 * Lombok (1.18.2)
 * Gitlab4j (4.8.30)

### Prerequisites
---
  + Install java 8 jdk ([http://www.oracle.com](http://www.oracle.com/technetwork/java/javase/downloads))

### Deploying docker image
---
  + alter build_and_publish.sh with custom ARTIFACTORY, PACKAGE and TAG variables
  + run build_and_publish.sh to automatically build and publish nmaas-platform image to selected artifactory
  
### Running Platform in local environment
---
  + Build and run the Platform with *./gradlew bootRun* in this directory.

### Running Platform on dedicated machine
---
  + Build the Platform with *./gradlew clean build* in this directory.
  + The output executable *nmaas-platform-x.x.x.jar* file is created in *nmaas-platform/build/libs* directory.
  + Run the Platform with *java -jar nmaas-platform-x.x.x.jar*.
  + Optionally run the Platform with additional argument *--Dlog4j.configurationFile* specifying the name of logger (Log4j2) configuration file (located in the same directory as the jar file) to be loaded instead of the built in one. Please be advised that you have to add that parameter before the *-jar* parameter.
  + It is advised to run the Platform with additional argument *--spring.config.name* specifying the name of the properties file (located in the same directory as the jar file) to be loaded instead of the built in one.

#### Populating Platform database with test data
---
  + To add pre-defined set of applications to the Platform run *nmaas-platform/src/test/shell/init.sh* script that will load NMaaS-compatible application definitions included in the *nmaas-platform/src/test/shell/data/apps* directory.
  + After running the initialisation script in the previous step, the *nmaas-platform/src/test/shell/populate-templates.sh* should be run to add configuration file templates for previously added applications.
  + Initial deployment environment data/configuration may be applied by running *nmaas-platform/src/test/shell/populate-inventory.sh* script that will load customer network, Kubernetes cluster and GitLab definitions included in the *nmaas-platform/src/test/shell/data/inventory* directory.

#### Complete deployment environment setup for Kubernetes
---
  A dedicated Helm chart has been developed to ease the NMaaS installation within the Kubernetes cluster. This way both the NM applications and the NMaaS core components can be installed within a Kubernetes cluster. For more information please contact the NMaaS Team.

#### Automated DCN setup with Ansible
---
  + Deploy a dedicated machine running Docker for executing Ansible playbooks on dedicated container for DCN/VPN configuration.
  + For DCN/VPN configuration it is assumed that there is a set of Docker container images available on one of the Docker Hosts that trigger Ansible playbooks responsible for core routers configuration.
