package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.api;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.repositories.DockerComposeFileTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/dockercompose/templates")
public class DockerComposeFileTemplateAdminRestController {

    @Autowired
    private DockerComposeFileTemplateRepository templates;

    /**
     * Lists all {@link DockerComposeFileTemplate} stored in repository.
     * @return list of {@link DockerComposeFileTemplate} objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')")
    @GetMapping(value = "")
    public List<DockerComposeFileTemplate> listAllDockerComposeFileTemplates() {
        return templates.findAll();
    }

    /**
     * Stores new {@link DockerComposeFileTemplate} in repository.
     * @param dockerComposeFileTemplate compose file template to be stored
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')")
    @PostMapping(value = "", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addDockerComposeFileTemplate(
            @RequestBody DockerComposeFileTemplate dockerComposeFileTemplate) {
        templates.save(dockerComposeFileTemplate);
    }

}
