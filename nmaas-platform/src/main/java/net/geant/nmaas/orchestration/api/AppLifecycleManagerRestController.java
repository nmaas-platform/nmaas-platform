package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
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

    @RequestMapping(value = "",
            params = {"clientid", "applicationid"},
            method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Identifier deployApplication(
            @RequestParam("clientid") String clientId,
            @RequestParam("applicationid") String applicationId) {
        return lifecycleManager.deployApplication(Identifier.newInstance(clientId), Identifier.newInstance(applicationId));
    }

    @RequestMapping(value = "/{deploymentId}",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.OK)
    public void applyConfiguration(
            @PathVariable("deploymentId") String deploymentId,
            @RequestBody String configuration) throws InvalidDeploymentIdException {
        lifecycleManager.applyConfiguration(Identifier.newInstance(deploymentId), new AppConfiguration(null, configuration));
    }

    @RequestMapping(value = "/{deploymentId}",
            method = RequestMethod.DELETE,
            consumes = "application/json")
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
