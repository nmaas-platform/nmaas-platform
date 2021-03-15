package net.geant.nmaas.portal.service.impl;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistent.entity.Configuration;
import net.geant.nmaas.portal.persistent.entity.InternationalizationSimple;
import net.geant.nmaas.portal.persistent.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.persistent.repositories.InternationalizationSimpleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.Optional;

@ApplicationScope
@AllArgsConstructor
@Component
public class ConfigurationManagerImpl implements ConfigurationManager {

    private final ConfigurationRepository repository;

    private final ModelMapper modelMapper;

    private final InternationalizationSimpleRepository internationalizationRepository;

    @Override
    public ConfigurationView getConfiguration(){
        return modelMapper.map(this.loadSingleConfiguration(), ConfigurationView.class);
    }

    @Override
    public Long setConfiguration(ConfigurationView configurationView) {
        if(repository.count() > 0){
            throw new OnlyOneConfigurationSupportedException("Configuration already exists. It can be either removed or updated");
        }
        Configuration configuration = modelMapper.map(configurationView, Configuration.class);
        this.repository.save(configuration);
        return configuration.getId();
    }

    @Override
    public void updateConfiguration(Long id, ConfigurationView updatedConfiguration) {
        Optional<Configuration> configuration = repository.findById(id);
        if(!configuration.isPresent()){
            throw new ConfigurationNotFoundException("Configuration with id "+id+" not found in repository");
        }
        InternationalizationSimple internationalization = internationalizationRepository.findByLanguageOrderByIdDesc(updatedConfiguration.getDefaultLanguage())
                .orElseThrow(()->new IllegalArgumentException("Language not found"));
        if(!internationalization.isEnabled()){
            throw new IllegalStateException("Default language must be active");
        }
        repository.save(modelMapper.map(updatedConfiguration, Configuration.class));
    }

    private Configuration loadSingleConfiguration(){
        if(repository.count() > 1 || repository.count() == 0){
            throw new IllegalStateException("Found "+repository.count()+" configuration instead of one");
        }
        return repository.findAll().get(0);
    }
}
