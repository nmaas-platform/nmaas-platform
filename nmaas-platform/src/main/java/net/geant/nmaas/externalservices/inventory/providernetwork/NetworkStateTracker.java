package net.geant.nmaas.externalservices.inventory.providernetwork;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservicedeployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import org.springframework.stereotype.Service;

/**
 * Keeps track of the network configuration applied for all the currently running NMaaS services, e.g. IP address
 * network pools and VLAN numbers already assigned on each of the Docker Hosts.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class NetworkStateTracker {

     public ContainerNetworkDetails prepareNetworkConfigForContainer(String serviceName, DockerHost host) {
         int vlanNumber = 0;

         return new ContainerNetworkDetails(
                 new ContainerNetworkIpamSpec("", "", ""),
                 vlanNumber);
     }

}
