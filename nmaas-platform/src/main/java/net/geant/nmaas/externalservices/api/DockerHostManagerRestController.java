package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.inventory.dockerhosts.*;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerHost;
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

    private DockerHostRepositoryManager dockerHostRepositoryManager;

    @Autowired
    public DockerHostManagerRestController(DockerHostRepositoryManager dockerHostRepositoryManager) {
        this.dockerHostRepositoryManager = dockerHostRepositoryManager;
    }

    /**
     * List all {@link DockerHost} instances
     * @return list of {@link DockerHost} instances
     */
    @RequestMapping(
            value = "",
            method = RequestMethod.GET)
    public List<DockerHost> listAllDockerHosts() {
        return dockerHostRepositoryManager.loadAll();
    }

    /**
     * Fetch {@link DockerHost} instance by name
     * @param name Unique {@link DockerHost} name
     * @return {@link DockerHost} instance
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "/{name}",
            method = RequestMethod.GET)
    public DockerHost getDockerHosts(
            @PathVariable("name") String name)
            throws DockerHostNotFoundException, DockerHostInvalidException {
        return dockerHostRepositoryManager.loadByName(name);
    }

    /**
     * Fetch first preferred {@link DockerHost} instance
     * @return {@link DockerHost} instance
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     */
    @RequestMapping(
            value = "/firstpreferred",
            method = RequestMethod.GET)
    public DockerHost getPreferedDockerHosts()
            throws DockerHostNotFoundException {
        return dockerHostRepositoryManager.loadPreferredDockerHost();
    }

    /**
     * Store {@link DockerHost} instance
     * @param newDockerHost new {@link DockerHost} instance
     * @throws DockerHostExistsException when Docker host exists (HttpStatus.CONFLICT)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addDockerHost(
            @RequestBody DockerHost newDockerHost) throws DockerHostExistsException, DockerHostInvalidException {
        dockerHostRepositoryManager.addDockerHost(newDockerHost);
    }

    /**
     * Update {@link DockerHost} instance
     * @param name Unique {@link DockerHost} name
     * @param dockerHost {@link DockerHost} instance pass to update
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "/{name}",
            method = RequestMethod.PUT,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateDockerHost(
            @PathVariable("name") String name,
            @RequestBody DockerHost dockerHost)
            throws DockerHostNotFoundException, DockerHostInvalidException {
        dockerHostRepositoryManager.updateDockerHost(name, dockerHost);
    }

    /**
     * Removes {@link DockerHost} instance
     * @param name Unique {@link DockerHost} name
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @RequestMapping(
            value = "/{name}",
            method = RequestMethod.DELETE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeDockerHost(
            @PathVariable("name") String name)
            throws DockerHostNotFoundException, DockerHostInvalidException {
        dockerHostRepositoryManager.removeDockerHost(name);
    }

    @ExceptionHandler(DockerHostNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleDockerHostNotFoundException(DockerHostNotFoundException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(DockerHostInvalidException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String handleDockerHostInvalidException(DockerHostInvalidException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(DockerHostExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDockerHostExistsException(DockerHostExistsException ex) {
        return ex.getMessage();
    }
 }
