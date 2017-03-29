package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RESTful API for managing Docker Host instances.
 *
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/dockerhosts")
public class DockerHostManagerRestController {

    private DockerHostRepository dockerHostRepository;

    @Autowired
    public DockerHostManagerRestController(DockerHostRepository dockerHostRepository) {
        this.dockerHostRepository = dockerHostRepository;
    }

    /**
     * List all {@link DockerHost} instances stored in the repository
     * @return list of {@link DockerHost} instances
     */
    @RequestMapping(
            value = "",
            method = RequestMethod.GET)
    public List<DockerHost> listAllDockerHosts() {
        return dockerHostRepository.loadAll();
    }

    /**
     * Fetch by name {@link DockerHost} instance from the repository
     * @param name Unique {@link DockerHost} name
     * @return {@link DockerHost} instance fetched form the repository
     * @throws {@link DockerHostNotFoundException}
     */
    @RequestMapping(
            value = "/{name}",
            method = RequestMethod.GET)
    public DockerHost getDockerHosts(
            @PathVariable("name") String name)
            throws DockerHostNotFoundException {
        return dockerHostRepository.loadByName(name);
    }

    /**
     * Fetch first preferred {@link DockerHost} instance from the repository
     * @return {@link DockerHost} instance fetched form the repository
     * @throws {@link DockerHostNotFoundException}
     */
    @RequestMapping(
            value = "/firstpreferred",
            method = RequestMethod.GET)
    public DockerHost getPreferedDockerHosts()
            throws DockerHostNotFoundException {
        return dockerHostRepository.loadPreferredDockerHost();
    }

    /**
     * Store {@link DockerHost} instance in the repository
     * @param newDockerHost new {@link DockerHost} instance
     */
    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addDockerHost(
            @RequestBody DockerHost newDockerHost) {
        dockerHostRepository.addDockerHost(newDockerHost);
    }

    /**
     * Update {@link DockerHost} instance in the repository
     * @param name Unique {@link DockerHost} name
     * @param dockerHost {@link DockerHost} instance pass to update
     * @throws {@link DockerHostNotFoundException}
     */
    @RequestMapping(
            value = "/{name}",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void updateDockerHost(
            @PathVariable("name") String name,
            @RequestBody DockerHost dockerHost)
            throws DockerHostNotFoundException {
        dockerHostRepository.updateDockerHost(name, dockerHost);
    }

    /**
     * Removes {@link DockerHost} instance from the repository
     * @param name Unique {@link DockerHost} name
     * @throws {@link DockerHostNotFoundException}
     */
    @RequestMapping(
            value = "/{name}",
            method = RequestMethod.DELETE)
    @ResponseStatus(code = HttpStatus.OK)
    public void removeDockerHost(
            @PathVariable("name") String name)
            throws DockerHostNotFoundException {
        dockerHostRepository.removeDockerHost(name);
    }
 }
