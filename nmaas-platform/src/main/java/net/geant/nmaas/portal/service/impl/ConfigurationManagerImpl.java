package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistent.entity.Configuration;
import net.geant.nmaas.portal.persistent.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.Optional;

@ApplicationScope
@Component
public class ConfigurationManagerImpl implements ConfigurationManager {

    private ConfigurationRepository repository;

    private ModelMapper modelMapper;

    @Autowired
    public ConfigurationManagerImpl(ConfigurationRepository repository, ModelMapper modelMapper){
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ConfigurationView getConfiguration(){
        return modelMapper.map(this.loadSingleConfiguration(), ConfigurationView.class);
    }

    @Override
    public Long addConfiguration(ConfigurationView configurationView) throws OnlyOneConfigurationSupportedException{
        if(repository.count() > 0){
            throw new OnlyOneConfigurationSupportedException("Configuration already exists. It can be either removed or updated");
        }
        Configuration configuration = modelMapper.map(configurationView, Configuration.class);
        this.repository.save(configuration);
        return configuration.getId();
    }

    @Override
    public void updateConfiguration(Long id, ConfigurationView updatedConfiguration) throws ConfigurationNotFoundException{
        Optional<Configuration> configuration = repository.findById(id);
        if(!configuration.isPresent()){
            throw new ConfigurationNotFoundException("Configuration with id "+id+" not found in repository");
        }
        repository.save(modelMapper.map(updatedConfiguration, Configuration.class));
    }

    private Configuration loadSingleConfiguration(){
        if(repository.count() != 1){
            throw new IllegalStateException("Found "+repository.count()+" configuration instead of one");
        }
        return repository.findAll().get(0);
    }

    @Override
    public void deleteAllConfigurations(){
        if(this.repository.count() > 0)
            this.repository.deleteAll();
    }
}
