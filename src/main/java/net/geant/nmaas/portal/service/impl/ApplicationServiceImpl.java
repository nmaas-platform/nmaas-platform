package net.geant.nmaas.portal.service.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.HelmChartRepositoryEmbeddable;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Log4j2
public class ApplicationServiceImpl implements ApplicationService {

	private final ApplicationRepository applicationRepository;

	@Override
	@Transactional
	public Application update(Application application) {
		// TODO checks
		checkApp(application);
		return applicationRepository.save(application);
	}

	@Override
	public void delete(Long id) {
		checkParam(id);
		applicationRepository.findById(id).ifPresent(app -> {
			if(app.getState().isChangeAllowed(ApplicationState.DELETED)){
				app.setState(ApplicationState.DELETED);
				applicationRepository.save(app);
			}
		});
	}

	@Override
	public Optional<Application> findApplication(Long id) {
		if (id != null)
			return applicationRepository.findById(id);
		else
			throw new IllegalArgumentException("applicationId is null");
	}

	@Override
	public Optional<Application> findApplication(String name, String version) {
		return this.applicationRepository.findByNameAndVersion(name, version);
	}

	@Override
	public Application findApplicationLatestVersion(String name) {
		if(!StringUtils.hasText(name)){
			throw new IllegalArgumentException("Application name cannot be null or empty");
		}
		return applicationRepository.findByName(name).stream()
				.max(Comparator.comparing(Application::getCreationDate))
				.orElseThrow(() -> new MissingElementException("Application " + name + " cannot be found"));
	}

	@Override
	public Page<Application> findAll(Pageable pageable) {
		return applicationRepository.findAll(pageable);
	}

	@Override
	public List<Application> findAll() {
		return applicationRepository.findAll();
	}

	@Override
	public void changeApplicationState(Application app, ApplicationState state){
		if(!app.getState().isChangeAllowed(state)){
			throw new IllegalStateException("Application state transition from " + app.getState() + " to " + state + " is not allowed.");
		}
		if(state.equals(ApplicationState.ACTIVE)){
			checkApp(app);
			checkTemplates(app);
		}
		app.setState(state);
		applicationRepository.save(app);
	}

	private void checkApp(Application app){
		if(app == null){
			throw new IllegalArgumentException("App cannot be null");
		}
		app.validate();
		app.getAppDeploymentSpec().validate();
		app.getAppDeploymentSpec().getKubernetesTemplate().validate();
		checkTemplates(app);
	}

	private void checkTemplates(Application app){
		if(app.getAppConfigurationSpec().isConfigFileRepositoryRequired()){
			app.getAppConfigurationSpec().getTemplates().forEach(this::validateConfigFileTemplates);
		}
	}

	private void validateConfigFileTemplates(ConfigFileTemplate configFileTemplate){
		try {
			new Template("test", configFileTemplate.getConfigFileTemplateContent(), new Configuration(Configuration.VERSION_2_3_28));
		} catch (IOException e) {
			throw new IllegalArgumentException("Template " + configFileTemplate.getConfigFileName() + " is invalid");
		}
	}

	@Override
	public void setMissingProperties(Application app, Long appId) {
		this.setMissingTemplatesId(app, appId);
	}

	private void checkParam(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}

	private void setMissingTemplatesId(Application app, Long appId){
		app.getAppConfigurationSpec().getTemplates()
				.forEach(template -> template.setApplicationId(appId));
	}

	public static void clearIds(Application app) {
		if(app.getConfigWizardTemplate() != null) {
			app.getConfigWizardTemplate().setId(null);
		}
		if(app.getConfigUpdateWizardTemplate() != null) {
			app.getConfigUpdateWizardTemplate().setId(null);
		}
		if(app.getAppConfigurationSpec() != null) {
			app.getAppConfigurationSpec().setId(null);
			app.getAppConfigurationSpec().getTemplates().forEach(a -> a.setId(null));
		}
		if(app.getAppDeploymentSpec() != null) {
			app.getAppDeploymentSpec().setId(null);
			app.getAppDeploymentSpec().getAccessMethods().forEach(a -> a.setId(null));
			app.getAppDeploymentSpec().getStorageVolumes().forEach(a -> a.setId(null));
			app.getAppDeploymentSpec().getKubernetesTemplate().setId(null);
			app.getAppDeploymentSpec().getKubernetesTemplate().getChart().setId(null);
		}
	}

	@Override
	public boolean exists(String name, String version) {
		return applicationRepository.existsByNameAndVersion(name, version);
	}

	@Override
	public Application create(Application application) {
		if(application.getId() != null) {
			throw new ProcessingException("While creating id must be null");
		}
		clearIds(application);
		return this.applicationRepository.save(application);
	}
}
