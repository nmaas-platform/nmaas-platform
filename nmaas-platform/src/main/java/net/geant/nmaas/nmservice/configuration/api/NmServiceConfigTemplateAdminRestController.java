package net.geant.nmaas.nmservice.configuration.api;

import lombok.AllArgsConstructor;
import net.geant.nmaas.nmservice.configuration.NmServiceConfigurationTemplateService;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.model.NmServiceConfigurationTemplateView;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/api/management/configurations/templates")
public class NmServiceConfigTemplateAdminRestController {

    private NmServiceConfigurationTemplateService templateService;

    /**
     * Lists all {@link NmServiceConfigurationTemplate} stored in repository.
     * @return list of {@link NmServiceConfigurationTemplateView} objects
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @GetMapping
    public List<NmServiceConfigurationTemplateView> listAllConfigurationTemplates() {
        return templateService.findAll();
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @GetMapping(value = "/{id}")
    public List<NmServiceConfigurationTemplateView> getAllConfigurationTemplatesByAppId(@PathVariable Long id) {
        return templateService.findAllByAppId(id);
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @PostMapping(value = "/validate")
    public void validateTemplate(@RequestBody List<NmServiceConfigurationTemplateView> configurationTemplate){
        templateService.validateTemplates(configurationTemplate);
    }

    /**
     * Stores new {@link NmServiceConfigurationTemplate} in repository.
     * @param configurationTemplate configuration template to be stored
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @PostMapping(consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addConfigurationTemplate(
            @RequestBody NmServiceConfigurationTemplateView configurationTemplate) {
        templateService.addTemplate(configurationTemplate);
    }

}
