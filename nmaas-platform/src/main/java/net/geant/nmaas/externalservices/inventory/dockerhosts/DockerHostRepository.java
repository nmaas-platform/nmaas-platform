package net.geant.nmaas.externalservices.inventory.dockerhosts;

import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores a static list of Docker Hosts available in the system.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Service
public class DockerHostRepository {

    public static final String ANSIBLE_DOCKER_HOST_NAME = "GN4-ANSIBLE-HOST";

    private List<DockerHost> dockerHosts = new ArrayList<>();

    {
        try {
            dockerHosts.add(new DockerHost(
                    "GN4-DOCKER-1",
                    InetAddress.getByName("10.134.250.1"),
                    2375,
                    InetAddress.getByName("10.134.250.1"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.11.0.0"),
                    "/home/mgmt/nmaasplatform/volumes",
                    true));
            dockerHosts.add(new DockerHost(
                    "GN4-DOCKER-2",
                    InetAddress.getByName("10.134.250.2"),
                    2375,
                    InetAddress.getByName("10.134.250.2"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.12.0.0"),
                    "/home/mgmt/nmaasplatform/volumes",
                    false));
            dockerHosts.add(new DockerHost(
                    "GN4-DOCKER-3",
                    InetAddress.getByName("10.134.250.3"),
                    2375,
                    InetAddress.getByName("10.134.250.3"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.13.0.0"),
                    "/home/mgmt/nmaasplatform/volumes",
                    false));
            dockerHosts.add(new DockerHost(
                    ANSIBLE_DOCKER_HOST_NAME,
                    InetAddress.getByName("10.134.250.6"),
                    2375,
                    InetAddress.getByName("10.134.250.6"),
                    "eth0",
                    "eth1",
                    InetAddress.getByName("10.16.0.0"),
                    "/home/mgmt/ansible/volumes",
                    false));
        } catch (UnknownHostException e) {
            System.out.println("Was not enable to complete assignment of static list of Docker Hosts");
        }
    }

    /**
     * Store {@link DockerHost} instance in the repository
     * @param newDockerHost New {@link DockerHost} instance
     * @throws DockerHostExistsException when Docker host exists in the repository
     * @throws DockerHostInvalidException when invalid input
     */
    public void addDockerHost(DockerHost newDockerHost) throws DockerHostExistsException, DockerHostInvalidException {
        validateDockerHost(newDockerHost);
        try {
            loadByName(newDockerHost.getName());
            throw new DockerHostExistsException("Docker host with " +  newDockerHost.getName() +  " name exists in the repository.");
        } catch (DockerHostNotFoundException ex) {
            this.dockerHosts.add(newDockerHost);
        }
    }

    /**
     * Remove {@link DockerHost} instance from the repository
     * @param name {@link DockerHost} instance
     * @throws DockerHostNotFoundException  when Docker host does not exists in the repository
     * @throws DockerHostInvalidException when invalid input
     */
    public void removeDockerHost(String name) throws DockerHostNotFoundException, DockerHostInvalidException {
        loadByName(name);
        dockerHosts.removeIf(p -> p.getName().equals(name));
    }

    /**
     * Update {@link DockerHost} instance in the repository
     * @param name Unique {@link DockerHost} name
     * @param dockerHost New {@link DockerHost} instance
     * @throws DockerHostNotFoundException  when Docker host does not exists in the repository
     * @throws DockerHostInvalidException when invalid input
     */
    public void updateDockerHost(String name, DockerHost dockerHost) throws DockerHostNotFoundException, DockerHostInvalidException {
        validateDockerHostAndName(name, dockerHost);
        this.dockerHosts.set(dockerHosts.indexOf(loadByName(name)), dockerHost);
    }

    /**
     * Loads all {@link DockerHost} instances
     * @return {@link List} of {@link DockerHost} instances
     */
    public List<DockerHost> loadAll() {
        return dockerHosts;
    }

    /**
     * Loads by name the {@link DockerHost} instance from the repository
     * @param hostName Unique {@link DockerHost} name
     * @return {@link DockerHost} instance loaded form the repository
     * @throws DockerHostNotFoundException  when Docker host does not exists in the repository
     * @throws DockerHostInvalidException when invalid input
     */
    public DockerHost loadByName(String hostName) throws DockerHostNotFoundException, DockerHostInvalidException {
        validateDockerHostName(hostName);
        return dockerHosts.stream()
                .filter((host) -> host.getName().equals(hostName))
                .findFirst()
                .orElseThrow(() -> new DockerHostNotFoundException("Did not find Docker host with " + hostName + " name in the repository"));
    }

    /**
     * Loads first preferred {@link DockerHost} instance from the repository
     * @return {@link DockerHost} instance loaded form the repository
     * @throws DockerHostNotFoundException when preferred Docker host does not exists in the repository
     */
    public DockerHost loadPreferredDockerHost() throws DockerHostNotFoundException {
        return dockerHosts.stream()
                .filter((host) -> host.isPreferred())
                .findFirst()
                .orElseThrow(() -> new DockerHostNotFoundException("Did not find preferred Docker host in the repository."));
    }

    protected void validateDockerHostName(String name) throws DockerHostInvalidException {
        if(name == null) {
            throw new DockerHostInvalidException("Docker host name cannot be null");
        }
    }

    protected void validateDockerHost(DockerHost dockerHost) throws DockerHostInvalidException {
        if(dockerHost == null) {
            throw new DockerHostInvalidException("Docker host cannot be null");
        }
        validateDockerHostName(dockerHost.getName());
    }

    protected void validateDockerHostAndName(String name, DockerHost dockerHost) throws DockerHostInvalidException {
        validateDockerHostName(name);
        validateDockerHost(dockerHost);
        if(!name.equals(dockerHost.getName())) {
            throw new DockerHostInvalidException("Docker host name has to be the same (name:" + name + ",dockerHost.name:" + dockerHost.getName());
        }
    }
}