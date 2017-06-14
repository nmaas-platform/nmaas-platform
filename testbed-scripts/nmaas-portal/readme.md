This directory contains a list of scripts launched (i.a. by Bamboo) on the central NMaaS Portal 
machine to manage NMaaS software prototype deployment in the testbed. Dedicated scripts are used to 
check, kill and run both NMaaS Platform and NMaaS Portal prototype components.

Scripts should be placed in /home/mgmt/scripts directory which corresponds to the default setting in 
the Bamboo deployment plan.

Note that if the kill_platform.sh script is launched with a parameter (e.g. kill_platform.sh clean) 
the clear_docker_hosts.sh script is launched with a lists of IP addresses of Docker Hosts as 
arguments (e.g. clear_docker_hosts.sh 10.134.250.1). This script connects to each of the specified 
Docker Hosts via SSH (keys must be exchanged before) and runs scripts responsible for docker 
containers and docker networks removal on that Host. 

Note that the run_platform.sh script will run two initialization scripts after launching the NMaaS 
Platform itself namely init.sh and populate-inventory.sh in the /home/mgmt/platform/init-and-tests 
directory. Those initialization scripts together with configuration json files should be obtained 
from the NMaaS git repository (from nmaas-platform/src/test/shell directory) and placed in the 
mentioned location. 

Note that the run_tests.sh script may be used to run NMaaS Platform system tests. It launches a 
test.sh script in the /home/mgmt/platform/init-and-tests directory also available from the NMaaS git 
repository.