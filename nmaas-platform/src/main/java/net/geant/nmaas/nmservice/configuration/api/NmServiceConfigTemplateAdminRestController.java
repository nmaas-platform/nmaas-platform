package net.geant.nmaas.nmservice.configuration.api;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileTemplatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RestController
@RequestMapping(value = "/platform/api/management/configurations/templates")
public class NmServiceConfigTemplateAdminRestController {

    @Autowired
    private NmServiceConfigFileTemplatesRepository templates;

    /**
     * Lists all {@link NmServiceConfigurationTemplate} stored in repository.
     * @return list of {@link NmServiceConfigurationTemplate} objects
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @GetMapping(value = "")
    public List<NmServiceConfigurationTemplate> listAllConfigurationTemplates() {
        return templates.findAll();
    }

    /**
     * Stores new {@link NmServiceConfigurationTemplate} in repository.
     * @param configurationTemplate configuration template to be stored
     */
    @PreAuthorize("hasRole('ROLE_SUPERADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @PostMapping(value = "", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addConfigurationTemplate(
            @RequestBody NmServiceConfigurationTemplate configurationTemplate) {
        templates.save(configurationTemplate);
    }

}
