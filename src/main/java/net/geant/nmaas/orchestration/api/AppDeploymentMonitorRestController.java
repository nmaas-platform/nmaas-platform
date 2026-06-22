package net.geant.nmaas.orchestration.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppDeploymentDto;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exposes REST API methods to retrieve information on application deployments
 */
@RestController
@RequestMapping(value = "/api/${nmaas.api.version:v1}/orchestration/deployments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Application Deployment Monitor", description = "Administrator API for retrieving application deployment details")
public class AppDeploymentMonitorRestController {

    private final AppDeploymentMonitor deploymentMonitor;
    private final ModelMapper modelMapper;

    /**
     * Retrieves information on all deployments, including their identifier and current state.
     *
     * @return list of deployments
     */
    @GetMapping(value = "")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public List<AppDeploymentDto> listAllDeployments() {
        return deploymentMonitor.allDeployments().stream()
                .map(d -> modelMapper.map(d, AppDeploymentDto.class))
                .collect(Collectors.toList());
    }

    /**
     * Returns the current state of a particular deployment.
     *
     * @param deploymentId application deployment identifier
     * @return current deployment state
     * @throws InvalidDeploymentIdException if deployment with the provided identifier doesn't exist in the system
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping(value = "{deploymentId}/state")
    public AppLifecycleState loadDeploymentState(@PathVariable String deploymentId) {
        return deploymentMonitor.state(Identifier.newInstance(deploymentId));
    }

    /**
     * Returns deployed application access information.
     *
     * @param deploymentId application deployment identifier
     * @return application access information
     * @throws InvalidDeploymentIdException if deployment with the provided identifier doesn't exist in the system
     * @throws InvalidAppStateException     if deployment didn't complete yet
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping(value = "{deploymentId}/access")
    public AppUiAccessDetails loadDeploymentUserAccessInfo(@PathVariable String deploymentId) {
        return deploymentMonitor.userAccessDetails(Identifier.newInstance(deploymentId));
    }

    @ExceptionHandler(InvalidDeploymentIdException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleInvalidDeploymentIdException(InvalidDeploymentIdException ex) {
        log.warn("Requested deployment not found -> {}", ex.getMessage());
        return ex.getMessage();
    }

    @ExceptionHandler(InvalidAppStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleInvalidAppStateException(InvalidAppStateException ex) {
        log.warn("Requested deployment in invalid state -> {}", ex.getMessage());
        return ex.getMessage();
    }

}
