package net.geant.nmaas.orchestration;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostState;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostStateKeeper;
import net.geant.nmaas.nmservice.DeploymentIdToNmServiceNameMapper;
import net.geant.nmaas.nmservice.deployment.nmservice.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class AppLifecycleRepository {

    @Autowired
    private NmServiceRepository serviceRepository;

    @Autowired
    private DeploymentIdToNmServiceNameMapper deploymentIdMapper;

    @Autowired
    private DockerHostStateKeeper dockerHostStateKeeper;

    private Map<Identifier, AppDeploymentState> deployments = new HashMap<>();

    public void storeNewDeployment(Identifier deploymentId) {
        deployments.put(deploymentId, AppDeploymentState.REQUESTED);
    }

    public void updateDeploymentState(Identifier deploymentId, AppDeploymentState currentState) {
        deployments.put(deploymentId, currentState);
    }

    public AppDeploymentState loadCurrentState(Identifier deploymentId) throws InvalidDeploymentIdException {
        AppDeploymentState deploymentState = deployments.get(deploymentId);
        if (deploymentState != null)
            return deploymentState;
        else
            throw new InvalidDeploymentIdException(
                    "Deployment with id " + deploymentId + " not found in the repository. ");
    }

    public boolean isDeploymentStored(Identifier deploymentId) {
        return (deployments.get(deploymentId) != null);
    }

    public AppUiAccessDetails loadAccessDetails(Identifier deploymentId) throws InvalidDeploymentIdException {
        try {
            String serviceName = deploymentIdMapper.nmServiceName(deploymentId);
            return accessDetails(serviceRepository.loadService(serviceName));
        } catch (DeploymentIdToNmServiceNameMapper.EntryNotFoundException
                | NmServiceRepository.ServiceNotFoundException
                | DockerHostNotFoundException e) {
            throw new InvalidDeploymentIdException(
                    "Deployment with id " + deploymentId + " not found in the repository. ");
        }
    }

    public AppUiAccessDetails accessDetails(NmServiceInfo serviceInfo) throws DockerHostNotFoundException {
        try {
            final DockerHost host = (DockerHost)serviceInfo.getHost();
            final String accessAddress = host.getAccessInterfaceName();
            final Integer accessPort;
            accessPort = dockerHostStateKeeper.getAssignedPort(host.getName(), serviceInfo.getName());
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append("http://").append(accessAddress).append(":").append(accessPort);
            return new AppUiAccessDetails(urlBuilder.toString());
        } catch (DockerHostState.MappingNotFoundException e) {
            throw new DockerHostNotFoundException("Problem with loading access port -> " + e.getMessage());
        }
    }

}
