package net.geant.nmaas.externalservices.inventory.network;

import net.geant.nmaas.externalservices.inventory.network.entities.DockerHostAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.exceptions.AttachPointNotFoundException;
import net.geant.nmaas.externalservices.inventory.network.repositories.DockerHostAttachPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping(value = "/api/management/network")
public class DockerHostAttachPointController {

    private DockerHostAttachPointRepository dockerHostAttachPointRepository;

    @Autowired
    public DockerHostAttachPointController(
            DockerHostAttachPointRepository dockerHostAttachPointRepository) {
        this.dockerHostAttachPointRepository = dockerHostAttachPointRepository;
    }

    @GetMapping("/dockerhosts")
    public List<DockerHostAttachPoint> listAllDockerHostAttachPoints() {
        return dockerHostAttachPointRepository.findAll();
    }

    @GetMapping("/dockerhosts/{name}")
    public DockerHostAttachPoint getDockerHostAttachPoint(
            @PathVariable("name") String name) {
        return dockerHostAttachPointRepository
                .findByDockerHostName(name)
                .orElseThrow(() -> new AttachPointNotFoundException(name));
    }

    @PostMapping(
            value = "/dockerhosts",
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addDockerHostAttachPoint(@RequestBody DockerHostAttachPoint dockerHostAttachPoint) {
        dockerHostAttachPointRepository.save(dockerHostAttachPoint);
    }

    @PutMapping(
            value = "/dockerhosts/{name}",
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateDockerHostAttachPoint(
            @PathVariable("name") String name,
            @RequestBody DockerHostAttachPoint modifiedDockerHostAttachPoint) {
        DockerHostAttachPoint currentDockerHostAttachPoint = dockerHostAttachPointRepository
                .findByDockerHostName(name)
                .orElseThrow(() -> new AttachPointNotFoundException(name));
        currentDockerHostAttachPoint.setRouterName(modifiedDockerHostAttachPoint.getRouterName());
        currentDockerHostAttachPoint.setRouterId(modifiedDockerHostAttachPoint.getRouterId());
        currentDockerHostAttachPoint.setRouterInterfaceName(modifiedDockerHostAttachPoint.getRouterInterfaceName());
        dockerHostAttachPointRepository.save(currentDockerHostAttachPoint);
    }

    @DeleteMapping("/dockerhosts/{name}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeDockerHostAttachPoint(@PathVariable("name") String name) {
        DockerHostAttachPoint dhap = dockerHostAttachPointRepository
                .findByDockerHostName(name)
                .orElseThrow(() -> new AttachPointNotFoundException(name));
        dockerHostAttachPointRepository.delete(dhap);
    }

    @ExceptionHandler(AttachPointNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAttachPointNotFoundException(AttachPointNotFoundException ex) {
        return "Did not find attach point with provided identifier -> " + ex.getMessage();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public String handleDataAccessException(DataAccessException ex) {
        return "Couldn't complete requested operation -> " + ex.getMessage();
    }

}
