package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmManager;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotDestroyNmServiceException;
import net.geant.nmaas.servicedeployment.exceptions.OrchestratorInternalErrorException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerApiClientFactory.client;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class ServicesClient {

    public String deployService(ServiceSpec service, DockerSwarmManager swarmManager)
            throws CouldNotConnectToOrchestratorException, CouldNotDeployNmServiceException, OrchestratorInternalErrorException {
        DockerClient apiClient = client(swarmManager.getApiUri());
        try {
            ServiceCreateResponse response = apiClient.createService(service);
            return response.id();
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Swarm -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException("Could not create given service -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new OrchestratorInternalErrorException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public void destroyService(String serviceId, DockerSwarmManager swarmManager)
            throws CouldNotDestroyNmServiceException, CouldNotConnectToOrchestratorException, OrchestratorInternalErrorException {
        DockerClient apiClient = client(swarmManager.getApiUri());
        try {
            apiClient.removeService(serviceId);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Swarm -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service " + serviceId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new OrchestratorInternalErrorException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public List<String> listServices(DockerSwarmManager swarmManager) throws OrchestratorInternalErrorException {
        DockerClient apiClient = client(swarmManager.getApiUri());
        try {
            final List<com.spotify.docker.client.messages.swarm.Service> services = apiClient.listServices();
            return services.stream().map(s -> s.spec().name()).collect(Collectors.toList());
        } catch (DockerException dockerException) {
            throw new OrchestratorInternalErrorException("Could not connect to Docker Swarm -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new OrchestratorInternalErrorException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

}
