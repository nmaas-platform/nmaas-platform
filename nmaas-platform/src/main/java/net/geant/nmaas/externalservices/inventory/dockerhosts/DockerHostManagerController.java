package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostAlreadyExistsException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.model.DockerHostDetails;
import net.geant.nmaas.externalservices.inventory.dockerhosts.model.DockerHostView;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RESTful API for managing Docker Host instances.
 */
@RestController
@RequestMapping(value = "/api/management/dockerhosts")
public class DockerHostManagerController {

    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private ModelMapper modelMapper;

    @Autowired
    public DockerHostManagerController(DockerHostRepositoryManager dockerHostRepositoryManager, ModelMapper modelMapper) {
        this.dockerHostRepositoryManager = dockerHostRepositoryManager;
        this.modelMapper = modelMapper;
    }

    /**
     * Lists all {@link DockerHost} instances represented by {@link DockerHostView} objects.
     * @return list of {@link DockerHostView} objects
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping
    public List<DockerHostView> listAllDockerHosts() {
        return dockerHostRepositoryManager.loadAll().stream()
                .map(dockerHost -> modelMapper.map(dockerHost, DockerHostView.class))
                .collect(Collectors.toList());
    }

    /**
     * Fetch {@link DockerHost} instance by name
     * @param name Unique {@link DockerHost} name
     * @return {@link DockerHostDetails} instance
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping("/{name}")
    public DockerHostDetails getDockerHost(
            @PathVariable("name") String name) {
        return modelMapper.map(dockerHostRepositoryManager.loadByName(name), DockerHostDetails.class);
    }

    /**
     * Fetch first preferred {@link DockerHost} instance
     * @return {@link DockerHostDetails} instance
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping("/firstpreferred")
    public DockerHostDetails getPreferredDockerHost() {
        return modelMapper.map(dockerHostRepositoryManager.loadPreferredDockerHost(), DockerHostDetails.class);
    }

    /**
     * Store new {@link DockerHost} instance
     * @param newDockerHost new {@link DockerHostDetails} data
     * @throws DockerHostAlreadyExistsException when Docker host exists (HttpStatus.CONFLICT)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PostMapping(value = "", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addDockerHost(@RequestBody DockerHostDetails newDockerHost) {
        dockerHostRepositoryManager.addDockerHost(modelMapper.map(newDockerHost, DockerHost.class));
    }

    /**
     * Update {@link DockerHost} instance
     * @param name Unique {@link DockerHost} name
     * @param dockerHost {@link DockerHost} instance pass to update
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PutMapping(value = "/{name}", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateDockerHost(
            @PathVariable("name") String name,
            @RequestBody DockerHostDetails dockerHost) {
        dockerHostRepositoryManager.updateDockerHost(name, modelMapper.map(dockerHost, DockerHost.class));
    }

    /**
     * Removes {@link DockerHost} instance
     * @param name Unique {@link DockerHost} name
     * @throws DockerHostNotFoundException when Docker host does not exists (HttpStatus.NOT_FOUND)
     * @throws DockerHostInvalidException when invalid input (HttpStatus.NOT_ACCEPTABLE)
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @DeleteMapping("/{name}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeDockerHost(@PathVariable("name") String name) {
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

    @ExceptionHandler(DockerHostAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDockerHostExistsException(DockerHostAlreadyExistsException ex) {
        return ex.getMessage();
    }

}
