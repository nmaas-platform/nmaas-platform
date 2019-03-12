package net.geant.nmaas.nmservice.configuration;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import net.geant.nmaas.nmservice.configuration.model.NmServiceConfigurationTemplateView;
import net.geant.nmaas.nmservice.configuration.repositories.NmServiceConfigFileTemplatesRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class NmServiceConfigurationTemplateServiceImpl implements NmServiceConfigurationTemplateService {

    private ModelMapper modelMapper;

    private NmServiceConfigFileTemplatesRepository repository;

    @Override
    @Transactional
    public List<NmServiceConfigurationTemplateView> findAll() {
        return repository.findAll().stream()
                .map(template -> modelMapper.map(template, NmServiceConfigurationTemplateView.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<NmServiceConfigurationTemplateView> findAllByAppId(Long appId) {
        return repository.findAllByApplicationId(appId).stream()
                .map(template -> modelMapper.map(template, NmServiceConfigurationTemplateView.class))
                .collect(Collectors.toList());
    }

    @Override
    public void validateTemplates(List<NmServiceConfigurationTemplateView> templates) {
        if(templates.isEmpty()){
            throw new IllegalArgumentException("Templates must be specified when repository is required");
        }
        templates.forEach(this::validateTemplate);
    }

    @Override
    @Transactional
    public void validateSubmittedTemplates(Long appId) {
        List<NmServiceConfigurationTemplateView> templates = repository.findAllByApplicationId(appId).stream()
                .map(template -> modelMapper.map(template, NmServiceConfigurationTemplateView.class))
                .collect(Collectors.toList());
        this.validateTemplates(templates);
    }

    @Override
    @Transactional
    public void addTemplate(NmServiceConfigurationTemplateView configurationTemplate) {
        validateTemplate(configurationTemplate);
        repository.save(modelMapper.map(configurationTemplate, NmServiceConfigurationTemplate.class));
    }

    private void validateTemplate(NmServiceConfigurationTemplateView configTemplate){
        try {
            new Template("test", configTemplate.getConfigFileTemplateContent(), new Configuration(Configuration.VERSION_2_3_28));
        } catch (IOException e) {
            throw new IllegalArgumentException("Template " + configTemplate.getConfigFileName() + " is invalid");
        }
    }
}
