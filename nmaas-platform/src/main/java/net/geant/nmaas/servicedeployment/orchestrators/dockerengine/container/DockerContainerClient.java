package net.geant.nmaas.servicedeployment.orchestrators.dockerengine.container;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotDeployNmServiceException;
import net.geant.nmaas.servicedeployment.exceptions.CouldNotDestroyNmServiceException;
import net.geant.nmaas.servicedeployment.exceptions.UnknownInternalException;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerEngineContainerTemplate;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.spotify.docker.client.messages.swarm.Task.Criteria;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DockerContainerClient {

    public String deploy(ContainerConfig container, String name, DockerHost host)
            throws CouldNotConnectToOrchestratorException, CouldNotDeployNmServiceException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(host.getApiUri()).build();
            ContainerCreation result = apiClient.createContainer(container, name);
            return result.id();
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException("Could not create given container -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public void remove(String containerId, DockerHost host)
            throws CouldNotConnectToOrchestratorException, CouldNotDestroyNmServiceException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(host.getApiUri()).build();
            apiClient.removeContainer(containerId);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Engine -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDestroyNmServiceException("Could not remove container" + containerId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public void pullImage(String imageName, DockerHost host)
            throws CouldNotDestroyNmServiceException, UnknownInternalException {
        DockerClient apiClient = null;
        try {
            apiClient = DefaultDockerClient.builder().uri(host.getApiUri()).build();
            apiClient.pull(imageName);
        } catch (DockerException dockerException) {
            throw new CouldNotDestroyNmServiceException("Could not pull image " + imageName + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        } finally {
            if (apiClient != null) apiClient.close();
        }
    }

    public void containers(DockerHost host) throws DockerException, InterruptedException {
        DockerClient apiClient = DefaultDockerClient.builder().uri(host.getApiUri()).build();
        System.out.println(apiClient.listContainers(DockerClient.ListContainersParam.allContainers()));
        apiClient.close();
    }

}
