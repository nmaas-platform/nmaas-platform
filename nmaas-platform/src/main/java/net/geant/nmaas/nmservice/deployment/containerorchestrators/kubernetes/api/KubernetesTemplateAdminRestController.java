package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.api;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.InternalErrorException;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KubernetesTemplateNotFoundException;
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
@Profile("env_kubernetes")
@RequestMapping(value = "/platform/api/management/apps/{appId}/kubernetes/template")
public class KubernetesTemplateAdminRestController {

    @Autowired
    private ApplicationRepository applications;

    /**
     * Loads Kubernetes template for given application.
     *
     * @param appId Identifier of application
     * @return Kubernetes template
     * @throws MissingElementException if application with given identifier is missing
     * @throws InternalErrorException if deployment spec is not set for given application
     * @throws KubernetesTemplateNotFoundException if kubernetes template is not set in given deployment spec
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')")
    @GetMapping(value = "")
    @Transactional
    public KubernetesTemplate getKubernetesTemplate(@PathVariable(value = "appId") Long appId)
            throws MissingElementException, InternalErrorException, KubernetesTemplateNotFoundException {
        Application app = applications.findOne(appId);
        if(app == null)
            throw new MissingElementException("Application with id " + appId + " not found.");
        AppDeploymentSpec appDeploymentSpec = app.getAppDeploymentSpec();
        if (appDeploymentSpec == null)
            throw new InternalErrorException("Application deployment spec for application with id " + appId + " is not set.");
        KubernetesTemplate template = appDeploymentSpec.getKubernetesTemplate();
        if (template == null)
            throw new KubernetesTemplateNotFoundException("Kubernetes template for application with id " + appId + " is missing.");
        return template;
    }

    /**
     * Stores new {@link KubernetesTemplate} in repository for given application.
     *
     * @param appId Identifier of application
     * @param kubernetesTemplate Kubernetes template to be stored
     * @throws MissingElementException if application with given identifier is missing
     * @throws InternalErrorException if deployment spec is not set for given application
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')")
    @PostMapping(value = "", consumes = "application/json")
    @Transactional
    @ResponseStatus(code = HttpStatus.CREATED)
    public void setDockerComposeFileTemplate(@PathVariable(value = "appId") Long appId,
            @RequestBody KubernetesTemplate kubernetesTemplate)
            throws MissingElementException, InternalErrorException {
        Application app = applications.findOne(appId);
        if(app == null)
            throw new MissingElementException("Application with id " + appId + " not found.");
        AppDeploymentSpec appDeploymentSpec = app.getAppDeploymentSpec();
        if (appDeploymentSpec == null)
            throw new InternalErrorException("Application deployment spec for application with id " + appId + " is not set.");
        appDeploymentSpec.setKubernetesTemplate(kubernetesTemplate);
        applications.save(app);
    }

    @ExceptionHandler(KubernetesTemplateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleComposeFileTemplateNotFoundException(KubernetesTemplateNotFoundException ex) {
        return ex.getMessage();
    }

}
