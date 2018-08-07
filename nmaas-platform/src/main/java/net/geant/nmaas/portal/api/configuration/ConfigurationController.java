package net.geant.nmaas.portal.api.configuration;

import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistent.entity.Configuration;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/configuration")
public class ConfigurationController {

    private ConfigurationManager configurationManager;

    @Autowired
    public ConfigurationController(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @GetMapping
    public Configuration getMaintenance(){
        return this.configurationManager.getConfiguration();
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Long addConfiguration(@RequestBody Configuration configuration) throws OnlyOneConfigurationSupportedException {
        this.configurationManager.addConfiguration(configuration);
        return configuration.getId();
    }

    @PutMapping(value="/{id}")
    @PreAuthorize("hasRole('ROLE_SUPERADMIN')")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateConfiguration(@PathVariable("id") Long id, @RequestBody Configuration configuration) throws ConfigurationNotFoundException{
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
