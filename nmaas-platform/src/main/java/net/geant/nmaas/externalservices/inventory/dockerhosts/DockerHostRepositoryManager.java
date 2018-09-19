package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostAlreadyExistsException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.repositories.DockerHostRepository;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages the persistence of Docker Hosts available in the system.
 */
@Component
public class DockerHostRepositoryManager {

    private DockerHostRepository repository;

    @Autowired
    public DockerHostRepositoryManager(DockerHostRepository repository){
        this.repository = repository;
    }

    /**
     * Store {@link DockerHost} instance in the repository
     *
     * @param newDockerHost New {@link DockerHost} instance
     * @throws DockerHostAlreadyExistsException when Docker host exists in the repository
     * @throws DockerHostInvalidException when invalid input
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addDockerHost(DockerHost newDockerHost) throws DockerHostAlreadyExistsException, DockerHostInvalidException {
        validateDockerHost(newDockerHost);
        try {
            loadByName(newDockerHost.getName());
            throw new DockerHostAlreadyExistsException("Docker host with " +  newDockerHost.getName() +  " name exists in the repository.");
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
        repository.delete(dockerHost);
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
     * @throws DockerHostNotFoundException when Docker host does not exists in the repository
     */
    public DockerHost loadByName(String hostName) throws DockerHostNotFoundException {
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

    private void validateDockerHostName(String name) throws DockerHostInvalidException {
        if(name == null) {
            throw new DockerHostInvalidException("Docker host name cannot be null");
        }
    }

    private void validateDockerHost(DockerHost dockerHost) throws DockerHostInvalidException {
        if(dockerHost == null) {
            throw new DockerHostInvalidException("Docker host cannot be null");
        }
        validateDockerHostName(dockerHost.getName());
    }

    private void validateDockerHostAndName(String name, DockerHost dockerHost) throws DockerHostInvalidException {
        validateDockerHostName(name);
        validateDockerHost(dockerHost);
        if(!name.equals(dockerHost.getName())) {
            throw new DockerHostInvalidException("Docker host name has to be the same (name:" + name + ",dockerHost.name:" + dockerHost.getName());
        }
    }
}