package net.geant.nmaas.portal.api.configuration;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/${nmaas.api.version:v1}/configuration")
@RequiredArgsConstructor
@Tag(name = "Platform Configuration", description = "Platform configuration management API")
public class ConfigurationController {

    private final ConfigurationManager configurationManager;

    @GetMapping
    public ConfigurationView getConfiguration() {
        return this.configurationManager.getConfiguration();
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long setConfiguration(@RequestBody @Valid ConfigurationView configuration) {
        return this.configurationManager.setConfiguration(configuration);
    }

    @PutMapping(value = "/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateConfiguration(@PathVariable("id") Long id, @RequestBody @Valid ConfigurationView configuration) {
        this.configurationManager.updateConfiguration(id, configuration);
    }

    @ExceptionHandler(OnlyOneConfigurationSupportedException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleOnlyOneConfigurationSupportedException(OnlyOneConfigurationSupportedException e) {
        return e.getMessage();
    }

    @ExceptionHandler(ConfigurationNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
    public String handleConfigurationNotFoundException(ConfigurationNotFoundException e) {
        return e.getMessage();
    }
}
