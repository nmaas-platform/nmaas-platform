This directory contains scripts that are used to remove all docker containers and (NMaaS defined) 
docker networks on particular Docker Host.

Scripts are triggered remotely from the central NMaaS Portal machine via SSH during the testbed 
preparation process before deployment of new version of NMaaS prototype software.

Scripts should be placed in /home/mgmt/scripts directory (default setting) on each Docker Host that 
might need to be cleaned during testbed preparation process.

Note that the docker network removal script will remove all networks for which macvlan driver was 
used. This is the default network driver used by the NMaaS prototype to connect containers to the 
testbed data plane.
