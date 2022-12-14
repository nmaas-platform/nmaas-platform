package net.geant.nmaas.portal.api.configuration;

import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/api/configuration")
public class ConfigurationController {

    private ConfigurationManager configurationManager;

    @Autowired
    public ConfigurationController(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @GetMapping
    public ConfigurationView getConfiguration(){
        return this.configurationManager.getConfiguration();
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long setConfiguration(@RequestBody @Valid ConfigurationView configuration) {
        return this.configurationManager.setConfiguration(configuration);
    }

    @PutMapping(value="/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateConfiguration(@PathVariable("id") Long id, @RequestBody @Valid ConfigurationView configuration) {
        this.configurationManager.updateConfiguration(id, configuration);
    }

    @ExceptionHandler(OnlyOneConfigurationSupportedException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleOnlyOneConfigurationSupportedException(OnlyOneConfigurationSupportedException e){
        return e.getMessage();
    }

    @ExceptionHandler(ConfigurationNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
    public String handleConfigurationNotFoundException(ConfigurationNotFoundException e){
        return e.getMessage();
    }
}
