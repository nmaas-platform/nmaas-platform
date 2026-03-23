package net.geant.nmaas.portal.api.apps;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.security.Principal;

@RestController
@RequestMapping(value = "/api/apps/instances")
@RequiredArgsConstructor
@Slf4j
public class AppConfigurationController {

    private static final String INSTANCE_NOT_FOUND_MESSAGE = "App instance not found";

    private final ApplicationInstanceService instances;
    private final AppLifecycleManager appLifecycleManager;
    private final JsonMapper jsonMapper;

    @PostMapping("/{appInstanceId}/configure")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void applyConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                   @RequestBody AppConfigurationView configuration,
                                   @NotNull Principal principal) {
        AppInstance appInstance = instances.find(appInstanceId).orElseThrow(() -> new MissingElementException(INSTANCE_NOT_FOUND_MESSAGE));

        boolean valid = validJSON(jsonMapper.writeValueAsString(configuration.getJsonInput()));
        log.debug("Provided configuration = {}", configuration.getJsonInput());
        if (!valid) {
            throw new ProcessingException("Configuration is not in a valid JSON format");
        }

        if (configuration.getStorageSpace() != null && configuration.getStorageSpace() <= 0) {
            throw new ProcessingException("Storage space cannot be less or equal to 0");
        }

        if (!instances.validateAgainstAppConfiguration(appInstance, configuration)) {
            throw new ProcessingException("Application configuration violates application state per domain rules");
        }

        appInstance.setConfiguration(jsonMapper.writeValueAsString(configuration.getJsonInput()));
        instances.update(appInstance);

        try {
            appLifecycleManager.applyConfiguration(appInstance.getInternalId(), configuration, principal.getName());
        } catch (Exception e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    private boolean validJSON(String json) {
        try {
            jsonMapper.readTree(json);
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }

    @PostMapping("/{appInstanceId}/configure/update")
    @PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'OWNER')")
    @Transactional
    public void updateConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId,
                                    @RequestBody AppConfigurationView configuration,
                                    @NotNull Principal principal) {
        AppInstance appInstance = instances.find(appInstanceId).orElseThrow(() -> new MissingElementException(INSTANCE_NOT_FOUND_MESSAGE));

        if (!StringUtils.isEmpty(jsonMapper.writeValueAsString(configuration.getJsonInput()))) {
            throw new ProcessingException("Configuration file content updates from the wizard are not supported");
        }

        if (!instances.validateAgainstAppConfiguration(appInstance, configuration)) {
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
    public String getConfiguration(@PathVariable(value = "appInstanceId") Long appInstanceId) {
        return instances.find(appInstanceId)
                .orElseThrow(() -> new MissingElementException(INSTANCE_NOT_FOUND_MESSAGE)).getConfiguration();
    }

}
