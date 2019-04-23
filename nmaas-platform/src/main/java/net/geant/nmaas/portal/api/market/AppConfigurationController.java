package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api")
public class AppConfigurationController {

    private static final String MISSING_APP_INSTANCE_MESSAGE = "Missing app instance";

    private static final String INSTANCE_NOT_FOUND_MESSAGE = "App instance not found";

    private ApplicationInstanceService instances;

    private AppLifecycleManager appLifecycleManager;


    @Autowired
    public AppConfigurationController(AppLifecycleManager appLifecycleManager,
                                 ApplicationInstanceService applicationInstanceService) {
        this.appLifecycleManager = appLifecycleManager;
        this.instances = applicationInstanceService;
    }


    private boolean validJSON(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @PostMapping({"/apps/instances/{appInstanceId}/configure", "/domains/{domainId}/apps/instances/{appInstanceId}/configure"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void applyConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                   @RequestBody AppConfigurationView configuration, @NotNull Principal principal) {
        AppInstance appInstance = instances.find(appInstanceId).orElseThrow(() -> new MissingElementException(INSTANCE_NOT_FOUND_MESSAGE));

        boolean valid = validJSON(configuration.getJsonInput());
        if (!valid)
            throw new ProcessingException("Configuration is not in valid JSON format");

        if(configuration.getStorageSpace() != null && configuration.getStorageSpace() <= 0)
            throw new ProcessingException("Storage space cannot be less or equal 0");

        appInstance.setConfiguration(configuration.getJsonInput());
        instances.update(appInstance);

        try {
            appLifecycleManager.applyConfiguration(appInstance.getInternalId(), configuration);
        } catch (Throwable e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @PostMapping({"/apps/instances/{appInstanceId}/configure/update", "/domains/{domainId}/apps/instances/{appInstanceId}/configure/update"})
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void updateConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                    @RequestBody AppConfigurationView configuration, @NotNull Principal principal) {
        AppInstance appInstance = instances.find(appInstanceId).orElseThrow(() -> new MissingElementException(INSTANCE_NOT_FOUND_MESSAGE));

        if (!validJSON(configuration.getJsonInput()))
            throw new ProcessingException("Configuration is not in valid JSON format");

        appInstance.setConfiguration(configuration.getJsonInput());
        instances.update(appInstance);

        try {
            appLifecycleManager.updateConfiguration(appInstance.getInternalId(), configuration);
        } catch (Exception e) {
            throw new ProcessingException(MISSING_APP_INSTANCE_MESSAGE);
        }
    }

    @GetMapping("/apps/instances/{appInstanceId}/configuration")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public String getConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId){
        return instances.find(appInstanceId).orElseThrow(() -> new MissingElementException(INSTANCE_NOT_FOUND_MESSAGE)).getConfiguration();
    }
}
