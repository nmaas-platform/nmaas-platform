package net.geant.nmaas.externalservices.api;

import net.geant.nmaas.externalservices.api.model.DockerHostDetails;
import net.geant.nmaas.externalservices.api.model.DockerHostMapper;
import net.geant.nmaas.externalservices.api.model.DockerHostView;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepositoryManager;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostAlreadyExistsException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
     * Lists all {@link DockerHost} instances represented by {@link DockerHostView} objects.
     * @return list of {@link DockerHostView} objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(
            value = "",
            method = RequestMethod.GET)
    public List<DockerHostView> listAllDockerHosts() {
        return dockerHostRepositoryManager.loadAll().stream()
                .map(dockerHost -> DockerHostMapper.toView(dockerHost))
                .collect(Collectors.toList());
    }

    /**
     * Fetch {@link DockerHost} instance by name
     * @param name Unique {@link DockerHost} name
     * @return {@link DockerHostDetails} instance
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(
            value = "/{name}",
            method = RequestMethod.GET)
    public DockerHostDetails getDockerHost(
            @PathVariable("name") String name)
            throws DockerHostNotFoundException, DockerHostInvalidException {
        return DockerHostMapper.toDetails(dockerHostRepositoryManager.loadByName(name));
    }

    /**
     * Fetch first preferred {@link DockerHost} instance
     * @return {@link DockerHostDetails} instance
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(
            value = "/firstpreferred",
            method = RequestMethod.GET)
    public DockerHostDetails getPreferredDockerHost()
            throws DockerHostNotFoundException {
        return DockerHostMapper.toDetails(dockerHostRepositoryManager.loadPreferredDockerHost());
    }

    /**
     * Store new {@link DockerHost} instance
     * @param newDockerHost new {@link DockerHostDetails} data
     * @throws DockerHostAlreadyExistsException when Docker host exists (HttpStatus.CONFLICT)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     * @throws DockerHostMapper.MapperException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addDockerHost(
            @RequestBody DockerHostDetails newDockerHost) throws DockerHostAlreadyExistsException, DockerHostInvalidException, DockerHostMapper.MapperException {
        dockerHostRepositoryManager.addDockerHost(DockerHostMapper.fromDetails(newDockerHost));
    }

    /**
     * Update {@link DockerHost} instance
     * @param name Unique {@link DockerHost} name
     * @param dockerHost {@link DockerHost} instance pass to update
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     * @throws DockerHostMapper.MapperException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(
            value = "/{name}",
            method = RequestMethod.PUT,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateDockerHost(
            @PathVariable("name") String name,
            @RequestBody DockerHostDetails dockerHost)
            throws DockerHostNotFoundException, DockerHostInvalidException, DockerHostMapper.MapperException {
        dockerHostRepositoryManager.updateDockerHost(name, DockerHostMapper.fromDetails(dockerHost));
    }

    /**
     * Removes {@link DockerHost} instance
     * @param name Unique {@link DockerHost} name
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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

    @ExceptionHandler(DockerHostMapper.MapperException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String handleDockerHostMapperException(DockerHostMapper.MapperException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(DockerHostAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDockerHostExistsException(DockerHostAlreadyExistsException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAccessDeniedException(AccessDeniedException ex) {
        return ex.getMessage();
    }
}
