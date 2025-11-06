package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.nmservice.deployment.bulks.BulkDeploymentJob;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.exceptions.ConfigurationNotFoundException;
import net.geant.nmaas.portal.exceptions.OnlyOneConfigurationSupportedException;
import net.geant.nmaas.portal.persistence.entity.Configuration;
import net.geant.nmaas.portal.persistence.entity.InternationalizationSimple;
import net.geant.nmaas.portal.persistence.repositories.ConfigurationRepository;
import net.geant.nmaas.portal.persistence.repositories.InternationalizationSimpleRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.scheduling.BulkDeploymentScheduleInit;
import net.geant.nmaas.scheduling.ScheduleManager;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.Optional;

@ApplicationScope
@RequiredArgsConstructor
@Component
public class ConfigurationManagerImpl implements ConfigurationManager {

    private final ConfigurationRepository repository;
    private final InternationalizationSimpleRepository internationalizationRepository;
    private final ScheduleManager scheduleManager;
    private final BulkDeploymentJob bulkDeploymentJob;
    private final ModelMapper modelMapper;

    @Override
    public ConfigurationView getConfiguration() {
        return modelMapper.map(this.loadSingleConfiguration(), ConfigurationView.class);
    }

    @Override
    public Long setConfiguration(ConfigurationView configurationView) {
        if (repository.count() > 0) {
            throw new OnlyOneConfigurationSupportedException("Configuration already exists. It can be either removed or updated");
        }
        Configuration configuration = modelMapper.map(configurationView, Configuration.class);
        this.repository.save(configuration);
        return configuration.getId();
    }

    @Override
    public void updateConfiguration(Long id, ConfigurationView updatedConfiguration) {
        Optional<Configuration> configuration = repository.findById(id);
        if (configuration.isEmpty()) {
            throw new ConfigurationNotFoundException("Configuration with id " + id + " not found in repository");
        }
        InternationalizationSimple internationalization = internationalizationRepository.findByLanguageOrderByIdDesc(updatedConfiguration.getDefaultLanguage())
                .orElseThrow(() -> new IllegalArgumentException("Language not found"));
        if (!internationalization.isEnabled()) {
            throw new IllegalStateException("Default language must be active");
        }
        if (!updatedConfiguration.getBulkDeploymentJobCron().equalsIgnoreCase(configuration.get().getBulkDeploymentJobCron())) {
            // job needs to be recreated
            scheduleManager.deleteJob(BulkDeploymentScheduleInit.BULK_DEPLOYMENT_JOB);
            scheduleManager.createJob(bulkDeploymentJob, BulkDeploymentScheduleInit.BULK_DEPLOYMENT_JOB, updatedConfiguration.getBulkDeploymentJobCron());
        }
        repository.save(modelMapper.map(updatedConfiguration, Configuration.class));
    }

    private Configuration loadSingleConfiguration() {
        if (repository.count() > 1 || repository.count() == 0) {
            throw new IllegalStateException("Found " + repository.count() + " configurations instead of one");
        }
        return repository.findAll().get(0);
    }
}
