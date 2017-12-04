package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.api;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities.DockerComposeFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.DockerComposeFileTemplateNotFoundException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.exceptions.InternalErrorException;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@Profile("docker-compose")
@RequestMapping(value = "/platform/api/management/apps/{appId}/dockercompose/template")
public class DockerComposeFileTemplateAdminRestController {

    @Autowired
    private ApplicationRepository applications;

    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_MANAGER')")
    @GetMapping(value = "")
    @Transactional
    public DockerComposeFileTemplate getDockerComposeFileTemplate(@PathVariable(value = "appId") Long appId)
            throws MissingElementException, InternalErrorException, DockerComposeFileTemplateNotFoundException {
        Application app = applications.findOne(appId);
        if(app == null)
            throw new MissingElementException("Application with id " + appId + " not found.");
        AppDeploymentSpec appDeploymentSpec = app.getAppDeploymentSpec();
        if (appDeploymentSpec == null)
            throw new InternalErrorException("Application deployment spec for application with id " + appId + " is not set.");
        DockerComposeFileTemplate template = appDeploymentSpec.getDockerComposeFileTemplate();
        if (template == null)
            throw new DockerComposeFileTemplateNotFoundException("Compose file for application with id " + appId + " is missing.");
        return template;
    }

    /**
     * Stores new {@link DockerComposeFileTemplate} in repository.
     * @param dockerComposeFileTemplate compose file template to be stored
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_MANAGER')")
    @PostMapping(value = "", consumes = "application/json")
    @Transactional
    @ResponseStatus(code = HttpStatus.CREATED)
    public void setDockerComposeFileTemplate(@PathVariable(value = "appId") Long appId,
            @RequestBody DockerComposeFileTemplate dockerComposeFileTemplate)
            throws MissingElementException, InternalErrorException {
        Application app = applications.findOne(appId);
        if(app == null)
            throw new MissingElementException("Application with id " + appId + " not found.");
        AppDeploymentSpec appDeploymentSpec = app.getAppDeploymentSpec();
        if (appDeploymentSpec == null)
            throw new InternalErrorException("Application deployment spec for application with id " + appId + " is not set.");
        appDeploymentSpec.setDockerComposeFileTemplate(dockerComposeFileTemplate);
        applications.save(app);
    }

    @ExceptionHandler(DockerComposeFileTemplateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleComposeFileTemplateNotFoundException(DockerComposeFileTemplateNotFoundException ex) {
        return ex.getMessage();
    }

}
