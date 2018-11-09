package net.geant.nmaas.orchestration.api;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.api.model.AppDeploymentView;
import net.geant.nmaas.orchestration.entities.AppLifecycleState;
import net.geant.nmaas.orchestration.entities.AppUiAccessDetails;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes REST API methods to retrieve information on application deployments.
 */
@RestController
@RequestMapping(value = "/api/orchestration/deployments")
@Log4j2
public class AppDeploymentMonitorRestController {

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
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
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
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @RequestMapping(value = "{deploymentId}/state", method = RequestMethod.GET)
    public AppLifecycleState loadDeploymentState(
            @PathVariable String deploymentId) {
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
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @RequestMapping(value = "{deploymentId}/access", method = RequestMethod.GET)
    public AppUiAccessDetails loadDeploymentUserAccessInfo(
            @PathVariable String deploymentId) {
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
