package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistent.entity.Configuration;
import net.geant.nmaas.portal.persistent.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.Optional;

@ApplicationScope
@Component
public class ConfigurationManagerImpl implements ConfigurationManager {

    @Autowired
    public ConfigurationManagerImpl(ConfigurationRepository repository){
        this.repository = repository;
    }

    private ConfigurationRepository repository;

    @Override
    public Configuration getConfiguration(){
        return this.loadSingleConfiguration();
    }

    @Override
    public void addConfiguration(Configuration configuration) throws OnlyOneConfigurationSupportedException{
        if(repository.count() > 0){
            throw new OnlyOneConfigurationSupportedException("Configuration already exists. It can be either removed or updated");
        }
        this.repository.save(configuration);
    }

    @Override
    public void updateConfiguration(Long id, Configuration updatedConfiguration) throws ConfigurationNotFoundException{
        Optional<Configuration> configuration = repository.findById(id);
        if(!configuration.isPresent()){
            throw new ConfigurationNotFoundException("Configuration with id "+id+" not found in repository");
        }
        repository.save(updatedConfiguration);
    }

    private Configuration loadSingleConfiguration(){
        if(repository.count() != 1){
            throw new IllegalStateException("Found "+repository.count()+" configuration instead of one");
        }
        return repository.findAll().get(0);
    }
}
