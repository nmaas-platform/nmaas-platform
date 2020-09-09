package net.geant.nmaas.portal.service.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.AllArgsConstructor;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationService;
import org.modelmapper.ModelMapper;
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
public class ApplicationServiceImpl implements ApplicationService {

	private ApplicationRepository appRepo;

	private ModelMapper modelMapper;

	@Override
	@Transactional
	public Application create(ApplicationView request, String owner) {
		checkParam(request, owner);
		Application app =  appRepo.save(new Application(request.getName(), request.getVersion(), owner));
		this.setMissingProperties(request, app.getId());
		request.setAppVersionId(app.getId());
		request.setOwner(owner);
		clearIds(request);
		app = modelMapper.map(request, Application.class);
		checkParam(app);
		return appRepo.save(app);
	}

	@Override
	public Application update(Application app) {
		checkApp(app);
		return appRepo.save(app);
	}

	@Override
	public void delete(Long id) {
		checkParam(id);
		appRepo.findById(id).ifPresent(app -> {
			if(app.getState().isChangeAllowed(ApplicationState.DELETED)){
				app.setState(ApplicationState.DELETED);
				appRepo.save(app);
			}
		});
	}

	@Override
	public Optional<Application> findApplication(Long applicationId) {
		if (applicationId != null)
			return appRepo.findById(applicationId);
		else
			throw new IllegalArgumentException("applicationId is null");
	}

	@Override
	public Application findApplicationLatestVersion(String name) {
		if(StringUtils.isEmpty(name)){
			throw new IllegalArgumentException("Application name cannot be null or empty");
		}
		return appRepo.findByName(name).stream()
				.max(Comparator.comparing(Application::getCreationDate))
				.orElseThrow(() -> new MissingElementException("Application " + name + " cannot be found"));
	}

	@Override
	public Page<Application> findAll(Pageable pageable) {
		return appRepo.findAll(pageable);
	}

	@Override
	public List<Application> findAll() {
		return appRepo.findAll();
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
		appRepo.save(app);
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
	public void setMissingProperties(ApplicationView app, Long appId){
		setMissingTemplatesId(app, appId);
	}

	private void checkParam(ApplicationView request, String owner) {
		if(request == null)
			throw new IllegalArgumentException("Request cannot be null");
		if(StringUtils.isEmpty(request.getName()))
			throw new IllegalArgumentException("name is null");
		if(!request.getName().matches("^[a-zA-Z0-9- ]+$"))
			throw new IllegalArgumentException("Name contains illegal arguments");
		if(StringUtils.isEmpty(request.getVersion()))
		    throw new IllegalArgumentException("version is null");
		if(StringUtils.isEmpty(owner))
			throw new IllegalArgumentException("Owner is null");
		if(request.getDescriptions() == null || request.getDescriptions().isEmpty()){
			throw new IllegalArgumentException("Descriptions cannot be null");
		}
		if(request.getConfigWizardTemplate() == null) {
			throw new IllegalArgumentException("ConfigTemplate must not be null");
		}
		if(request.getAppDeploymentSpec() == null) {
			throw new IllegalArgumentException("AppDeploymentSpec must not be null");
		}
		if(request.getAppConfigurationSpec() == null) {
			throw new IllegalArgumentException("AppConfigurationSpec must not be null");
		}
		if(appRepo.existsByNameAndVersion(request.getName(), request.getVersion()))
		    throw new IllegalStateException("Application " + request.getName() + " in version " + request.getVersion() + " already exists.");
	}
	
	private void checkParam(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}
	
	private void checkParam(Application app) {
		if(app == null)
			throw new IllegalArgumentException("app is null");
		app.validate();
	}

	private void setMissingTemplatesId(ApplicationView app, Long appId){
		app.getAppConfigurationSpec().getTemplates()
				.forEach(template -> template.setApplicationId(appId));
	}

	private void clearIds(ApplicationView app) {
		app.getConfigWizardTemplate().setId(null);
		if(app.getConfigUpdateWizardTemplate() != null) {
			app.getConfigUpdateWizardTemplate().setId(null);
		}

		app.getAppConfigurationSpec().setId(null);
		app.getAppConfigurationSpec().getTemplates().forEach(a -> a.setId(null));

		app.getAppDeploymentSpec().setId(null);
		app.getAppDeploymentSpec().getAccessMethods().forEach(a -> a.setId(null));
		app.getAppDeploymentSpec().getStorageVolumes().forEach(a -> a.setId(null));
		app.getAppDeploymentSpec().getKubernetesTemplate().setId(null);
		app.getAppDeploymentSpec().getKubernetesTemplate().getChart().setId(null);
	}
}
