
# NMaaS Platform (backend)

NMaaS Platform implements mechanisms for on-demand NM service deployment in the cloud environment and connectivity setup between the managed equipment and the running service. 

NM services are containerized and deployed using [Docker](https://www.docker.com/).

Platform exposes two types of API, namely Java-based and REST webservice.

### Technologies
---
 * Java 8
 * Spring 4
 * Spring Boot (1.4.2)
 * spotify:docker-client (8.3.0)

### Prerequisites
 ---
  + install java 8 jdk 
    - [http://www.oracle.com](http://www.oracle.com/technetwork/java/javase/downloads)
  + deploy a set of machines running the Docker CE
    - [https://store.docker.com](https://store.docker.com/editions/community/docker-ce-server-ubuntu)
  + enable Docker Remote API connections by adding appropriate line in Docker configuartion file
    - *DOCKER_OPTS="-H tcp://IP_ADDRESS:2375 -H unix:///var/run/docker.sock"* in */etc/default/docker* file
    
### Build and run
---
  + in order to build the Platform run *gradlew clean build* in reactor directory
  + the output *nmaas-platform-0.1.jar* file is stored in *nmaas-platform/build/libs* directory
  + launch the Platform with *java -jar nmaas-platform-0.1.jar*

### Notes
 ---
  * in order to speed up random value generation process which is used by the SSH library it is advised to set the following line *securerandom.source=file:/dev/urandom* in *$JAVA_HOME/jre/lib/security/java.security* file.