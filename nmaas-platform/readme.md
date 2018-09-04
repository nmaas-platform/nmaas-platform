# NMaaS Platform (backend)

##### NMaaS Platform implements mechanisms for on-demand NM service deployment in the cloud environment and connectivity setup between the managed equipment and the running service. 

###### NM services are containerized and deployed using [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/overview/).
###### For secure connectivity setup between the NM service and monitored/managed customer equipment a set of [Ansible](https://www.ansible.com/) playbooks is executed.

###### Platform exposes two types of API, namely Java-based and REST webservice.
#
### Technologies
---
 * Java 8
 * Spring 5
 * Spring Boot (2.0.4)
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
  
### Local environment setup
---
  + Build and run the Platform with *gradlew bootRun* in reactor directory.
  + Please be advised that terminal or command line must be opened while using Platform.
  + To add pre-defined set of applications to the Platform run *nmaas-platform/src/test/shell/init.sh* script that will load NMaaS-compatible application definitions included in the *nmaas-platform/src/test/shell/data/apps* directory.
  + After running the initialisation script in the previous step, the *nmaas-platform/src/test/shell/populate-templates.sh* should be run to add configuration file and Docker Compose file templates for previously added applications.
  + Initial deployment environment data/configuration may be applied by running *nmaas-platform/src/test/shell/populate-inventory.sh* script that will load Docker Host and customer network definitions included in the *nmaas-platform/src/test/shell/data/inventory* directory.

### Production environment setup
  
#### Build and run
---
  + Build the Platform with *gradlew clean build* in reactor directory.
  + The output executable *nmaas-platform-0.5.0.jar* file is created in *nmaas-platform/build/libs* directory.
  + Run the Platform with *java -jar nmaas-platform-0.5.0.jar*.
  + It is optional to run the Platform with additional argument *--Dlog4j.configurationFile* specifying the name of logger (Log4j2) configuration file (located in the same directory as the jar file) to be loaded instead of the built in one. Please be advised that you have to add that parameter before *-jar* parameter.
  + It is advised to run the Platform with additional argument *--spring.config.name* specifying the name of the properties file (located in the same directory as the jar file) to be loaded instead of the built in one.
  + To add pre-defined set of applications to the Platform run *nmaas-platform/src/test/shell/init.sh* script that will load NMaaS-compatible application definitions included in the *nmaas-platform/src/test/shell/data/apps* directory.
  + After running the initialisation script in the previous step, the *nmaas-platform/src/test/shell/populate-templates.sh* should be run to add configuration file and Docker Compose file templates for previously added applications.
  + Initial deployment environment data/configuration may be applied by running *nmaas-platform/src/test/shell/populate-inventory.sh* script that will load Docker Host and customer network definitions included in the *nmaas-platform/src/test/shell/data/inventory* directory.

#### Complete deployment environment setup
---
  + Deploy a dedicated machine to run NMaaS Platform software (section *Build and run*)
  + Deploy a set of machines (Docker Hosts) running the [Docker CE](https://store.docker.com/editions/community/docker-ce-server-ubuntu) (version 17.05.0-ce or later) and (if required) [Docker Compose](https://docs.docker.com/compose/install) (version 1.14.0 or later)
  + On each Docker Host enable Docker Remote API connections by adding line *DOCKER_OPTS="-H tcp://IP_ADDRESS:2375 -H unix:///var/run/docker.sock"* in Docker configuration file (*/etc/default/docker*)
  + Deploy a dedicated machine running Docker for executing Ansible playbooks for DCN/VPN configuration.

### Notes
---
  + It is assumed that the SSH communication between the machine on which the NMaaS software is running and the Docker Hosts is configured to use pre-exchanged SSH keys and no password is required.
  + For DCN/VPN configuration it is assumed that there is a set of Docker container images available on one of the Docker Hosts that trigger Ansible playbooks responsible for core routers configuration.
  + In order to speed up random value generation process which is used by the SSH library (on the machine hosting the NMaaS software) it is advised to set the following line *securerandom.source=file:/dev/urandom* in *$JAVA_HOME/jre/lib/security/java.security* file.