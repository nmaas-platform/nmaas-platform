package net.geant.nmaas.nmservice.configuration.api;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.repository.NmServiceConfigurationTemplatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/configs")
public class NmServiceConfigTemplateAdminRestController {

    @Autowired
    private NmServiceConfigurationTemplatesRepository templates;

    /**
     * Lists all {@link NmServiceConfigurationTemplate} stored in repository.
     * @return list of {@link NmServiceConfigurationTemplate} objects
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')")
    @RequestMapping(
            value = "",
            method = RequestMethod.GET)
    public List<NmServiceConfigurationTemplate> listAllConfigurationTemplates() {
        return templates.findAll();
    }

    /**
     * Stores new {@link NmServiceConfigurationTemplate} in repository.
     * @param configurationTemplate template to be stored
     */
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')")
    @RequestMapping(
            value = "",
            method = RequestMethod.POST,
            consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addConfigurationTemplate(
            @RequestBody NmServiceConfigurationTemplate configurationTemplate) {
        templates.save(configurationTemplate);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAccessDeniedException(AccessDeniedException ex) {
        return ex.getMessage();
    }

}
