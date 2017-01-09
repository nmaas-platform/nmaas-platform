package net.geant.nmaas.orchestrators.dockerswarm.service;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import net.geant.nmaas.exception.CouldNotConnectToOrchestratorException;
import net.geant.nmaas.exception.CouldNotDeployNmServiceException;
import net.geant.nmaas.exception.CouldNotDestroyNmServiceException;
import net.geant.nmaas.exception.UnknownInternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.spotify.docker.client.messages.swarm.Task.Criteria;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class ServicesManager {

    @Autowired
    private DockerClient docker;

    public String deployService(ServiceSpec service) throws CouldNotConnectToOrchestratorException, CouldNotDeployNmServiceException, UnknownInternalException {
        try {
            ServiceCreateResponse response = docker.createService(service);
            return response.id();
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Swarm -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDeployNmServiceException("Could not create given service -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        }
    }

    public void destroyService(String serviceId) throws CouldNotConnectToOrchestratorException, CouldNotDestroyNmServiceException, UnknownInternalException {
        try {
            docker.removeService(serviceId);
        } catch (DockerTimeoutException dockerTimeoutException) {
            throw new CouldNotConnectToOrchestratorException("Could not connect to Docker Swarm -> " + dockerTimeoutException.getMessage(), dockerTimeoutException);
        } catch (DockerException dockerException) {
            throw new CouldNotDestroyNmServiceException("Could not destroy service " + serviceId + " -> " + dockerException.getMessage(), dockerException);
        } catch (InterruptedException interruptedException) {
            throw new UnknownInternalException("Internal error -> " + interruptedException.getMessage(), interruptedException);
        }
    }

    public void tasks() throws DockerException, InterruptedException {

        System.out.println(docker.listTasks());

        System.out.println(docker.listTasks(Criteria.builder().withServiceName("test-tomcat-2").build()));

    }

    public List<String> listServices() {
        try {
            final List<com.spotify.docker.client.messages.swarm.Service> services;
            services = docker.listServices();
            return services.stream().map(s -> s.spec().name()).collect(Collectors.toList());
        } catch (DockerException | InterruptedException e ) {
            e.printStackTrace();
            //throw new NotFoundException("Failed to retrieve services");
            return null;
        }
    }

}
