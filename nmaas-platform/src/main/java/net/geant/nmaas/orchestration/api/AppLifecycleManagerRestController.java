package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes REST API methods to manage application deployment lifecycle.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/api/orchestration/deployments")
public class AppLifecycleManagerRestController {

    private AppLifecycleManager lifecycleManager;

    private ApplicationRepository appRepo;

    @Autowired
    AppLifecycleManagerRestController(AppLifecycleManager lifecycleManager, ApplicationRepository appRepo) {
        this.lifecycleManager = lifecycleManager;
        this.appRepo = appRepo;
    }

    /**
     * Requests new application deployment.
     *
     * @param domain name of the client domain for this deployment
     * @param applicationId identifier of the application
     * @return unique identifier of the application deployment
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PostMapping(params = {"domain", "applicationid"})
    @ResponseStatus(code = HttpStatus.CREATED)
    public Identifier deployApplication(
            @RequestParam("domain") String domain,
            @RequestParam("applicationid") String applicationId,
            @RequestParam("deploymentname") String deploymentName) {
        boolean gitLabRequired = this.appRepo.findById(Long.parseLong(applicationId)).orElseThrow(()-> new IllegalArgumentException("Application not found")).isGitLabRequired();
        return lifecycleManager.deployApplication(domain, Identifier.newInstance(applicationId), deploymentName, gitLabRequired);
    }

    /**
     * Applies application configuration provided by the user for given deployment.
     *
     * @param deploymentId unique identifier of the application deployment
     * @param configuration initial application configuration provided by the user
     * @throws InvalidDeploymentIdException if deployment with provided identifier doesn't exist in the system
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @PostMapping(value = "/{deploymentId}", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.OK)
    public void applyConfiguration(
            @PathVariable("deploymentId") String deploymentId,
            @RequestBody String configuration) throws InvalidDeploymentIdException {
        lifecycleManager.applyConfiguration(Identifier.newInstance(deploymentId), new AppConfiguration(configuration));
    }

    /**
     * Requests application instance removal.
     *
     * @param deploymentId unique identifier of the application deployment
     * @throws InvalidDeploymentIdException if deployment with provided identifier doesn't exist in the system
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @DeleteMapping(value = "/{deploymentId}")
    @ResponseStatus(code = HttpStatus.OK)
    public void removeApplication(
            @PathVariable("deploymentId") String deploymentId) throws InvalidDeploymentIdException {
        lifecycleManager.removeApplication(Identifier.newInstance(deploymentId));
    }

    @ExceptionHandler(InvalidDeploymentIdException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleInvalidDeploymentIdException(InvalidDeploymentIdException ex) {
        System.out.println("Requested deployment not found -> " + ex.getMessage());
        return ex.getMessage();
    }

}
