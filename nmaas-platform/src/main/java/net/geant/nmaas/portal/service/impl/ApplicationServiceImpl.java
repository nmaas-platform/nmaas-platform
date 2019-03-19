package net.geant.nmaas.portal.service.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.domain.ConfigFileTemplateView;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationService;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class ApplicationServiceImpl implements ApplicationService {

	private ApplicationRepository appRepo;

	private ModelMapper modelMapper;

	@Override
	public Application create(ApplicationView request, String owner) {
		checkParam(request, owner);
		Application app =  appRepo.save(new Application(request.getName(), request.getVersion(), owner));
		this.setMissingProperties(request);
		modelMapper.map(request, app);
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
		appRepo.findById(id).ifPresent((app) -> {
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

	@Override
	@Transactional
	public List<ConfigFileTemplateView> getConfigFileTemplates(Long applicationId){
		return appRepo.findById(applicationId).orElseThrow(() -> new IllegalStateException("Application not found"))
				.getAppConfigurationSpec()
				.getTemplates().stream()
				.map(template -> modelMapper.map(template, ConfigFileTemplateView.class))
				.collect(Collectors.toList());
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
	public void setMissingProperties(ApplicationView app){
		setMissingDescriptions(app);
	}

	private void checkParam(ApplicationView request, String owner) {
		if(request == null)
			throw new IllegalArgumentException("Request cannot be null");
		if(StringUtils.isEmpty(request.getName()))
			throw new IllegalArgumentException("name is null");
		if(!request.getName().matches("^[a-zA-Z0-9-]+$"))
			throw new IllegalArgumentException("Name contains illegal arguments");
		if(StringUtils.isEmpty(request.getVersion()))
		    throw new IllegalArgumentException("version is null");
		if(StringUtils.isEmpty(owner))
			throw new IllegalArgumentException("Owner is null");
		if(request.getDescriptions() == null || request.getDescriptions().isEmpty()){
			throw new IllegalArgumentException("Descriptions cannot be null");
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

	private void setMissingDescriptions(ApplicationView app){
		AppDescriptionView appDescription = app.getDescriptions().stream()
				.filter(description -> description.getLanguage().equals("en"))
				.findFirst().orElseThrow(() -> new IllegalStateException("English description is missing"));
		app.getDescriptions()
				.forEach(description ->{
					if(StringUtils.isEmpty(description.getBriefDescription())){
						description.setBriefDescription(appDescription.getBriefDescription());
					}
					if(StringUtils.isEmpty(description.getFullDescription())){
						description.setFullDescription(appDescription.getFullDescription());
					}
				});
	}
}
