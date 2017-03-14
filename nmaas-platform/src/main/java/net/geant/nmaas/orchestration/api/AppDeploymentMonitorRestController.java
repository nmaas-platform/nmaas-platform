package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.orchestration.*;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/orchestration/deployments")
public class AppDeploymentMonitorRestController {

    private AppDeploymentMonitor deploymentMonitor;

    @Autowired
    public AppDeploymentMonitorRestController(AppDeploymentMonitor deploymentMonitor) {
        this.deploymentMonitor = deploymentMonitor;
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public Map<Identifier, AppLifecycleState> listAllDeployments() {
        return deploymentMonitor.allDeployments();
    }

    @RequestMapping(value = "{deploymentId}/state", method = RequestMethod.GET)
    public AppLifecycleState loadDeploymentState(
            @PathVariable String deploymentId) throws InvalidDeploymentIdException {
        return deploymentMonitor.state(Identifier.newInstance(deploymentId));
    }

    @RequestMapping(value = "{deploymentId}/access", method = RequestMethod.GET)
    public AppUiAccessDetails loadDeploymentUserAccessInfo(
            @PathVariable String deploymentId) throws InvalidDeploymentIdException, InvalidAppStateException {
        return deploymentMonitor.userAccessDetails(Identifier.newInstance(deploymentId));
    }

    @ExceptionHandler(InvalidDeploymentIdException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleInvalidDeploymentIdException(InvalidDeploymentIdException ex) {
        System.out.println("Requested deployment not found -> " + ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidAppStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleInvalidAppStateException(InvalidAppStateException ex) {
        System.out.println("Requested deployment in invalid state -> " + ex.getMessage());
        return ex.getMessage();
    }

}
