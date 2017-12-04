package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
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
@RequestMapping(value = "/platform/api/orchestration/deployments")
public class AppLifecycleManagerRestController {

    private AppLifecycleManager lifecycleManager;

    @Autowired
    public AppLifecycleManagerRestController(AppLifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
    }

    /**
     * Requests new application deployment.
     *
     * @param clientId identifier of the user/client requesting the deployment
     * @param applicationId identifier of the application
     * @return unique identifier of the application deployment
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @RequestMapping(value = "",
            params = {"clientid", "applicationid"},
            method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Identifier deployApplication(
            @RequestParam("clientid") String clientId,
            @RequestParam("applicationid") String applicationId) {
        return lifecycleManager.deployApplication(Identifier.newInstance(clientId), Identifier.newInstance(applicationId));
    }

    /**
     * Applies application configuration provided by the user/client for given deployment.
     *
     * @param deploymentId unique identifier of the application deployment
     * @param configuration initial application configuration provided by the user/client
     * @throws InvalidDeploymentIdException if deployment with provided identifier doesn't exist in the system
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @RequestMapping(value = "/{deploymentId}",
            method = RequestMethod.POST,
            consumes = "application/json")
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
    @RequestMapping(value = "/{deploymentId}",
            method = RequestMethod.DELETE)
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
