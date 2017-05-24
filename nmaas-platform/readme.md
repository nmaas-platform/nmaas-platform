
# NMaaS Platform (backend)

##### NMaaS Platform implements mechanisms for on-demand NM service deployment in the cloud environment and connectivity setup between the managed equipment and the running service. 

###### NM services are containerized and deployed using [Docker](https://www.docker.com/).
###### For connectivity setup a set of [Ansible](https://www.ansible.com/) playbooks is executed.

###### Platform exposes two types of API, namely Java-based and REST webservice.

#### Technologies
---
 * Java 8
 * Spring 4
 * Spring Boot (1.4.2)
 * spotify:docker-client (8.3.0)

#### Prerequisites
---
  + Install java 8 jdk 
    - [http://www.oracle.com](http://www.oracle.com/technetwork/java/javase/downloads)
  + Deploy a set of machines running the Docker CE (version 17.05.0-ce or later)
    - [https://store.docker.com](https://store.docker.com/editions/community/docker-ce-server-ubuntu)
  + Enable Docker Remote API connections by adding appropriate line in Docker configuration file
    - *DOCKER_OPTS="-H tcp://IP_ADDRESS:2375 -H unix:///var/run/docker.sock"* in */etc/default/docker* file
    
#### Build and run
---
  + In order to build the Platform run *gradlew clean build* in reactor directory.
  + The output *nmaas-platform-0.1.jar* file is stored in *nmaas-platform/build/libs* directory.
  + Launch the Platform with *java -jar nmaas-platform-0.1.jar*.
  + It is advised to run the platform with additional argument *--spring.config.name* specifying the name of the properties file (located in the same directory as the jar file) to be loaded instead of the built in one.
  
#### Notes
---
  * It is assumed that the SSH communication between the machine on which the NMaaS software is running and the Docker Hosts is configured to use pre-exchanged SSH keys and no password is required.
  * For DCN/VPN configuration it is assumed that a there is a set of Docker container images available on one of the Docker Hosts that trigger Ansible playbooks responsible for core routers configuration.
  * In order to speed up random value generation process which is used by the SSH library it is advised to set the following line *securerandom.source=file:/dev/urandom* in *$JAVA_HOME/jre/lib/security/java.security* file.