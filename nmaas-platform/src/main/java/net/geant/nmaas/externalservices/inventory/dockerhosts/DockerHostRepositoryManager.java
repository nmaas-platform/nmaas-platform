package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Manages the persistence of Docker Hosts available in the system.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
public class DockerHostRepositoryManager {

    public static final String ANSIBLE_DOCKER_HOST_NAME = "GN4-ANSIBLE-HOST";

    @Autowired
    private DockerHostRepository repository;

    @PostConstruct
    public void storeDefaultDockerHosts() throws UnknownHostException, DockerHostInvalidException {
        try {
            addDockerHost(new DockerHost(
                    "GN4-DOCKER-1",
                    InetAddress.getByName("10.134.250.1"),
                    2375,
                    InetAddress.getByName("10.134.250.1"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.11.0.0"),
                    "/home/mgmt/nmaasplatform/volumes",
                    true));
            addDockerHost(new DockerHost(
                    "GN4-DOCKER-2",
                    InetAddress.getByName("10.134.250.2"),
                    2375,
                    InetAddress.getByName("10.134.250.2"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.12.0.0"),
                    "/home/mgmt/nmaasplatform/volumes",
                    false));
            addDockerHost(new DockerHost(
                    "GN4-DOCKER-3",
                    InetAddress.getByName("10.134.250.3"),
                    2375,
                    InetAddress.getByName("10.134.250.3"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.13.0.0"),
                    "/home/mgmt/nmaasplatform/volumes",
                    false));
            addDockerHost(new DockerHost(
                    ANSIBLE_DOCKER_HOST_NAME,
                    InetAddress.getByName("10.134.250.6"),
                    2375,
                    InetAddress.getByName("10.134.250.6"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.16.0.0"),
                    "/home/mgmt/ansible/volumes",
                    false));
        } catch (DockerHostExistsException e) {
            // nothing to do
        }
    }

    /**
     * Store {@link DockerHost} instance in the repository
     *
     * @param newDockerHost New {@link DockerHost} instance
     * @throws DockerHostExistsException when Docker host exists in the repository
     * @throws DockerHostInvalidException when invalid input
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addDockerHost(DockerHost newDockerHost) throws DockerHostExistsException, DockerHostInvalidException {
        validateDockerHost(newDockerHost);
        try {
            loadByName(newDockerHost.getName());
            throw new DockerHostExistsException("Docker host with " +  newDockerHost.getName() +  " name exists in the repository.");
        } catch (DockerHostNotFoundException ex) {
            repository.save(newDockerHost);
        }
    }

    /**
     * Remove {@link DockerHost} instance from the repository
     *
     * @param name {@link DockerHost} instance
     * @throws DockerHostNotFoundException  when Docker host does not exists in the repository
     * @throws DockerHostInvalidException when invalid input
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeDockerHost(String name) throws DockerHostNotFoundException, DockerHostInvalidException {
        DockerHost dockerHost = loadByName(name);
        repository.delete(dockerHost.getId());
    }

    /**
     * Update {@link DockerHost} instance in the repository
     *
     * @param name Unique {@link DockerHost} name
     * @param dockerHost New {@link DockerHost} instance
     * @throws DockerHostNotFoundException  when Docker host does not exists in the repository
     * @throws DockerHostInvalidException when invalid input
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDockerHost(String name, DockerHost dockerHost) throws DockerHostNotFoundException, DockerHostInvalidException {
        validateDockerHostAndName(name, dockerHost);
        DockerHost currentDockerHost = loadByName(name);
        dockerHost.setId(currentDockerHost.getId());
        repository.save(dockerHost);
    }

    /**
     * Loads all {@link DockerHost} instances
     *
     * @return {@link List} of {@link DockerHost} instances
     */
    public List<DockerHost> loadAll() {
        return repository.findAll();
    }

    /**
     * Loads by name the {@link DockerHost} instance from the repository
     *
     * @param hostName Unique {@link DockerHost} name
     * @return {@link DockerHost} instance loaded form the repository
     * @throws DockerHostNotFoundException  when Docker host does not exists in the repository
     * @throws DockerHostInvalidException when invalid input
     */
    public DockerHost loadByName(String hostName) throws DockerHostNotFoundException, DockerHostInvalidException {
        validateDockerHostName(hostName);
        return repository.findByName(hostName)
                .orElseThrow(() -> new DockerHostNotFoundException("Did not find Docker Host with " + hostName + " name in the repository"));
    }

    /**
     * Loads first preferred {@link DockerHost} instance from the repository.
     * Note that this method should be replaced with more sophisticated selection algorithm.
     *
     * @return {@link DockerHost} instance loaded form the repository
     * @throws DockerHostNotFoundException when preferred Docker host does not exists in the repository
     */
    public DockerHost loadPreferredDockerHost() throws DockerHostNotFoundException {
        Iterable<DockerHost> preferredHosts = repository.findByPreferredTrue();
        if (preferredHosts.iterator().hasNext())
            return preferredHosts.iterator().next();
        else
            throw new DockerHostNotFoundException("Did not find preferred Docker host in the repository.");
    }

    /**
     * Loads default {@link DockerHost} instance to be used for Docker Compose deployments.
     *
     * @return {@link DockerHost} instance loaded form the repository
     * @throws DockerHostNotFoundException when preferred Docker host does not exists in the repository
     */
    public DockerHost loadPreferredDockerHostForDockerCompose() throws DockerHostNotFoundException {
        return repository.findByName("GN4-DOCKER-2").orElseThrow(() -> new DockerHostNotFoundException("Did not find preferred Docker host in the repository."));
    }

    void validateDockerHostName(String name) throws DockerHostInvalidException {
        if(name == null) {
            throw new DockerHostInvalidException("Docker host name cannot be null");
        }
    }

    void validateDockerHost(DockerHost dockerHost) throws DockerHostInvalidException {
        if(dockerHost == null) {
            throw new DockerHostInvalidException("Docker host cannot be null");
        }
        validateDockerHostName(dockerHost.getName());
    }

    void validateDockerHostAndName(String name, DockerHost dockerHost) throws DockerHostInvalidException {
        validateDockerHostName(name);
        validateDockerHost(dockerHost);
        if(!name.equals(dockerHost.getName())) {
            throw new DockerHostInvalidException("Docker host name has to be the same (name:" + name + ",dockerHost.name:" + dockerHost.getName());
        }
    }
}