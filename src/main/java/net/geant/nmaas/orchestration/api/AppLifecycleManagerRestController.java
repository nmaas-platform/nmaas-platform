package net.geant.nmaas.orchestration.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.security.Principal;

/**
 * Exposes REST API methods to manage application deployment lifecycle.
 */
@RestController
@RequestMapping(value = "/api/orchestration/deployments")
@RequiredArgsConstructor
@Log4j2
public class AppLifecycleManagerRestController {

    private final AppLifecycleManager lifecycleManager;
    private final ApplicationRepository appRepo;

    /**
     * Requests new application deployment.
     *
     * @param domain name of the client domain for this deployment
     * @param applicationId identifier of the application
     * @return unique identifier of the application deployment
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PostMapping(params = {"domain", "applicationid"})
    @ResponseStatus(code = HttpStatus.CREATED)
    public Identifier deployApplication(
            @RequestParam("domain") String domain,
            @RequestParam("applicationid") String applicationId,
            @RequestParam("deploymentname") String deploymentName) {
        Application app = this.appRepo.findById(Long.parseLong(applicationId)).orElseThrow(()-> new IllegalArgumentException("Application not found"));
        AppDeployment appDeployment = AppDeployment.builder()
                .domain(domain)
                .applicationId(Identifier.newInstance(applicationId))
                .deploymentName(deploymentName)
                .appName(app.getName())
                .build();
        return lifecycleManager.deployApplication(appDeployment);
    }

    /**
     * Applies application configuration provided by the user for given deployment.
     *
     * @param deploymentId unique identifier of the application deployment
     * @param configuration initial application configuration provided by the user
     * @throws InvalidDeploymentIdException if deployment with provided identifier doesn't exist in the system
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PostMapping(value = "/{deploymentId}", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.OK)
    public void applyConfiguration(
            @PathVariable("deploymentId") String deploymentId,
            @RequestBody AppConfigurationView configuration,
            @NotNull Principal principal) throws Throwable {
        lifecycleManager.applyConfiguration(
                Identifier.newInstance(deploymentId),
                configuration,
                principal.getName()
        );
    }

    /**
     * Updates application configuration
     *
     * @param deploymentId unique identifier of the application deployment
     * @param configuration initial application configuration provided by the user
     * @throws InvalidDeploymentIdException if deployment with provided identifier doesn't exist in the system
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PostMapping(value = "/{deploymentId}/update")
    @ResponseStatus(code = HttpStatus.OK)
    public void updateConfiguration(@PathVariable("deploymentId") String deploymentId,
                                    @RequestBody AppConfigurationView configuration) {
        lifecycleManager.updateConfiguration(Identifier.newInstance(deploymentId), configuration);
    }

    /**
     * Requests application instance removal.
     *
     * @param deploymentId unique identifier of the application deployment
     * @throws InvalidDeploymentIdException if deployment with provided identifier doesn't exist in the system
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @DeleteMapping(value = "/{deploymentId}")
    @ResponseStatus(code = HttpStatus.OK)
    public void removeApplication(@PathVariable("deploymentId") String deploymentId) {
        lifecycleManager.removeApplication(Identifier.newInstance(deploymentId));
    }

    @ExceptionHandler(InvalidDeploymentIdException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleInvalidDeploymentIdException(InvalidDeploymentIdException ex) {
        log.error("Requested deployment not found -> " + ex.getMessage());
        return ex.getMessage();
    }

}
