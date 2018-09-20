package net.geant.nmaas.externalservices.inventory.network;

import net.geant.nmaas.externalservices.inventory.network.entities.DockerHostAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.exceptions.AttachPointNotFoundException;
import net.geant.nmaas.externalservices.inventory.network.repositories.DockerHostAttachPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/management/network")
public class DockerHostAttachPointManagerRestController {

    private DockerHostAttachPointRepository dockerHostAttachPointRepository;

    @Autowired
    public DockerHostAttachPointManagerRestController(
            DockerHostAttachPointRepository dockerHostAttachPointRepository) {
        this.dockerHostAttachPointRepository = dockerHostAttachPointRepository;
    }

    @RequestMapping(
            value = "/dockerhosts",
            method = RequestMethod.GET)
    public List<DockerHostAttachPoint> listAllDockerHostAttachPoints() {
        return dockerHostAttachPointRepository.findAll();
    }

    @RequestMapping(
            value = "/dockerhosts/{name}",
            method = RequestMethod.GET)
    public DockerHostAttachPoint getDockerHostAttachPoint(
            @PathVariable("name") String name) throws AttachPointNotFoundException {
        return dockerHostAttachPointRepository
                .findByDockerHostName(name)
                .orElseThrow(() -> new AttachPointNotFoundException(name));
    }

    @RequestMapping(
            value = "/dockerhosts",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addDockerHostAttachPoint(
            @RequestBody DockerHostAttachPoint dockerHostAttachPoint) throws DataAccessException {
        dockerHostAttachPointRepository.save(dockerHostAttachPoint);
    }

    @RequestMapping(
            value = "/dockerhosts/{name}",
            method = RequestMethod.PUT,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateDockerHostAttachPoint(
            @PathVariable("name") String name,
            @RequestBody DockerHostAttachPoint modifiedDockerHostAttachPoint) throws AttachPointNotFoundException, DataAccessException {
        DockerHostAttachPoint currentDockerHostAttachPoint = dockerHostAttachPointRepository
                .findByDockerHostName(name)
                .orElseThrow(() -> new AttachPointNotFoundException(name));
        currentDockerHostAttachPoint.setRouterName(modifiedDockerHostAttachPoint.getRouterName());
        currentDockerHostAttachPoint.setRouterId(modifiedDockerHostAttachPoint.getRouterId());
        currentDockerHostAttachPoint.setRouterInterfaceName(modifiedDockerHostAttachPoint.getRouterInterfaceName());
        dockerHostAttachPointRepository.save(currentDockerHostAttachPoint);
    }

    @RequestMapping(
            value = "/dockerhosts/{name}",
            method = RequestMethod.DELETE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeDockerHostAttachPoint(
            @PathVariable("name") String name) throws DataAccessException, AttachPointNotFoundException {
        DockerHostAttachPoint dhap = dockerHostAttachPointRepository
                .findByDockerHostName(name)
                .orElseThrow(() -> new AttachPointNotFoundException(name));
        dockerHostAttachPointRepository.delete(dhap);
    }

    @ExceptionHandler(AttachPointNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAttachPointNotFoundException (AttachPointNotFoundException ex) {
        return "Did not find attach point with provided identifier -> " + ex.getMessage();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String handleDataAccessException(DataAccessException ex) {
        return "Couldn't complete requested operation -> " + ex.getMessage();
    }

}
