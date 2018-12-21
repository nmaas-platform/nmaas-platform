package net.geant.nmaas.nmservice.configuration.api;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.model.NmServiceConfigurationTemplateView;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileTemplatesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/management/configurations/templates")
public class NmServiceConfigTemplateAdminRestController {

    private NmServiceConfigFileTemplatesRepository templates;

    private ModelMapper modelMapper;

    @Autowired
    public NmServiceConfigTemplateAdminRestController(NmServiceConfigFileTemplatesRepository templates, ModelMapper modelMapper){
        this.templates = templates;
        this.modelMapper = modelMapper;
    }

    /**
     * Lists all {@link NmServiceConfigurationTemplate} stored in repository.
     * @return list of {@link NmServiceConfigurationTemplate} objects
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @GetMapping(value = "")
    public List<NmServiceConfigurationTemplate> listAllConfigurationTemplates() {
        return templates.findAll();
    }

    /**
     * Stores new {@link NmServiceConfigurationTemplate} in repository.
     * @param configurationTemplate configuration template to be stored
     */
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_TOOL_MANAGER')")
    @PostMapping(value = "", consumes = "application/json")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void addConfigurationTemplate(
            @RequestBody NmServiceConfigurationTemplateView configurationTemplate) {
        templates.save(modelMapper.map(configurationTemplate, NmServiceConfigurationTemplate.class));
    }

}
