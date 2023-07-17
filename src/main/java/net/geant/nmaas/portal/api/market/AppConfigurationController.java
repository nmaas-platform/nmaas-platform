package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api/apps/instances")
@Slf4j
public class AppConfigurationController {

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

    @PostMapping("/{appInstanceId}/configure")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void applyConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                   @RequestBody AppConfigurationView configuration, @NotNull Principal principal) {
        AppInstance appInstance = instances.find(appInstanceId).orElseThrow(() -> new MissingElementException(INSTANCE_NOT_FOUND_MESSAGE));

        boolean valid = validJSON(configuration.getJsonInput());
        log.error("Configuration = " + configuration.getJsonInput());
        if (!valid) {
            throw new ProcessingException("Configuration is not in valid JSON format");
        }

        if(configuration.getStorageSpace() != null && configuration.getStorageSpace() <= 0) {
            throw new ProcessingException("Storage space cannot be less or equal 0");
        }

        if(!instances.validateAgainstAppConfiguration(appInstance, configuration)) {
            throw new ProcessingException("Application configuration violates application state per domain rules");
        }

        appInstance.setConfiguration(configuration.getJsonInput());
        instances.update(appInstance);

        try {
            appLifecycleManager.applyConfiguration(appInstance.getInternalId(), configuration, principal.getName());
        } catch (Throwable e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    @PostMapping("/{appInstanceId}/configure/update")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void updateConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                    @RequestBody AppConfigurationView configuration, @NotNull Principal principal) {
        AppInstance appInstance = instances.find(appInstanceId).orElseThrow(() -> new MissingElementException(INSTANCE_NOT_FOUND_MESSAGE));

        if (!Strings.isNullOrEmpty(configuration.getJsonInput())) {
            throw new ProcessingException("Configuration file content updates from the wizard are not supported");
        }

        if(!instances.validateAgainstAppConfiguration(appInstance, configuration)) {
            throw new ProcessingException("Application configuration violates application state per domain rules");
        }

        try {
            appLifecycleManager.updateConfiguration(appInstance.getInternalId(), configuration);
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    @GetMapping("/{appInstanceId}/configuration")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public String getConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId){
        return instances.find(appInstanceId).orElseThrow(() -> new MissingElementException(INSTANCE_NOT_FOUND_MESSAGE)).getConfiguration();
    }
}
