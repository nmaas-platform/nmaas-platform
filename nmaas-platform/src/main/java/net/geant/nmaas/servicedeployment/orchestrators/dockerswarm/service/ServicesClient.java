package net.geant.nmaas.servicedeployment.orchestrators.dockerswarm.service;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import net.geant.nmaas.externalservices.inventory.dockerswams.DockerSwarmManager;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotDestroyNmServiceException;
import net.geant.nmaas.servicedeployment.exceptions.UnknownInternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.spotify.docker.client.messages.swarm.Task.Criteria;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class ServicesClient {

    public String deployService(ServiceSpec service, DockerSwarmManager swarmManager)
            throws CouldNotConnectToOrchestratorException, CouldNotDeployNmServiceException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(swarmManager.getApiUri()).build();
            ServiceCreateResponse response = apiClient.createService(service);
            return response.id();
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Swarm -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException("Could not create given service -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public void destroyService(String serviceId, DockerSwarmManager swarmManager)
            throws CouldNotConnectToOrchestratorException, CouldNotDestroyNmServiceException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(swarmManager.getApiUri()).build();
            apiClient.removeService(serviceId);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Swarm -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service " + serviceId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public void tasks(DockerSwarmManager swarmManager) throws DockerException, InterruptedException {
        DockerClient apiClient = DefaultDockerClient.builder().uri(swarmManager.getApiUri()).build();
        System.out.println(apiClient.listTasks());
        System.out.println(apiClient.listTasks(Criteria.builder().withServiceName("test-tomcat-2").build()));
        apiClient.close();
    }

    public List<String> listServices(DockerSwarmManager swarmManager) {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(swarmManager.getApiUri()).build();
            final List<com.spotify.docker.client.messages.swarm.Service> services;
            services = apiClient.listServices();
            return services.stream().map(s -> s.spec().name()).collect(Collectors.toList());
        } catch (DockerException | InterruptedException e ) {
            e.printStackTrace();
            //throw new NotFoundException("Failed to retrieve services");
            return null;
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

}
