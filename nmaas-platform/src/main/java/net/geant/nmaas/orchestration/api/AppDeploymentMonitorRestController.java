package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.api.model.AppDeploymentView;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exposes REST API methods to retrieve information on application deployments.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/orchestration/deployments")
public class AppDeploymentMonitorRestController {

    private final static Logger log = LogManager.getLogger(AppDeploymentMonitorRestController.class);

    private AppDeploymentMonitor deploymentMonitor;

    private ModelMapper modelMapper;

    @Autowired
    public AppDeploymentMonitorRestController(AppDeploymentMonitor deploymentMonitor, ModelMapper modelMapper) {
        this.deploymentMonitor = deploymentMonitor;
        this.modelMapper = modelMapper;
    }

    /**
     * Retrieves information on all deployments including their identifier and current state.
     *
     * @return list of deployments
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<AppDeploymentView> listAllDeployments() {
        return deploymentMonitor.allDeployments().stream()
                .map(d -> modelMapper.map(d, AppDeploymentView.class))
                .collect(Collectors.toList());
    }

    /**
     * Returns current state of particular deployment.
     *
     * @param deploymentId application deployment identifier
     * @return current deployment state
     * @throws InvalidDeploymentIdException if deployment with provided identifier doesn't exist in the system
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "{deploymentId}/state", method = RequestMethod.GET)
    public AppLifecycleState loadDeploymentState(
            @PathVariable String deploymentId) throws InvalidDeploymentIdException {
        return deploymentMonitor.state(Identifier.newInstance(deploymentId));
    }

    /**
     * Returns deployed application access information.
     *
     * @param deploymentId application deployment identifier
     * @return application access information
     * @throws InvalidDeploymentIdException if deployment with provided identifier doesn't exist in the system
     * @throws InvalidAppStateException if deployment didn't complete yet
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "{deploymentId}/access", method = RequestMethod.GET)
    public AppUiAccessDetails loadDeploymentUserAccessInfo(
            @PathVariable String deploymentId) throws InvalidDeploymentIdException, InvalidAppStateException {
        return deploymentMonitor.userAccessDetails(Identifier.newInstance(deploymentId));
    }

    @ExceptionHandler(InvalidDeploymentIdException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleInvalidDeploymentIdException(InvalidDeploymentIdException ex) {
        log.warn("Requested deployment not found -> " + ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidAppStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleInvalidAppStateException(InvalidAppStateException ex) {
        log.warn("Requested deployment in invalid state -> " + ex.getMessage());
        return ex.getMessage();
    }

}
