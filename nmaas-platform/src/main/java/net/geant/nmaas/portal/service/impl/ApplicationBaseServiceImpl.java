package net.geant.nmaas.portal.service.impl;

import lombok.AllArgsConstructor;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ApplicationBaseServiceImpl implements ApplicationBaseService {

    private ApplicationBaseRepository appBaseRepository;

    private ModelMapper modelMapper;

    private ApplicationStatePerDomainService applicationStatePerDomainService;

    @Override
    public ApplicationBase createApplicationOrAddNewVersion(ApplicationView application) {
        if(appBaseRepository.existsByName(application.getName())){
            return addNewAppVersion(application);
        }
        return createAppBase(application);
    }

    private ApplicationBase addNewAppVersion(ApplicationView application){
        ApplicationBase applicationBase = this.findByName(application.getName());
        applicationBase.getVersions().add(createAppVersion(application));
        applicationBase.validate();
        return appBaseRepository.save(applicationBase);
    }

    private ApplicationVersion createAppVersion(ApplicationView app){
        return ApplicationVersion.builder()
                .appVersionId(app.getId())
                .state(app.getState())
                .version(app.getVersion())
                .build();
    }

    private ApplicationBase createAppBase(ApplicationView application){
        this.setMissingDescriptions(application);
        ApplicationBase appBase = modelMapper.map(application, ApplicationBase.class);
        appBase.validate();
        appBase.getVersions().add(createAppVersion(application));
        ApplicationBase newApplicationBase = appBaseRepository.save(appBase);
        applicationStatePerDomainService.updateAllDomainsWithNewApplicationBase(newApplicationBase);
        return newApplicationBase;
    }

    @Override
    public void updateApplicationBase(ApplicationView application) {
        this.setMissingDescriptions(application);
        ApplicationBase appBase = modelMapper.map(application, ApplicationBase.class);
        updateApplicationBase(appBase);
    }

    @Override
    public void updateApplicationBase(ApplicationBase application){
        application.validate();
        appBaseRepository.save(application);
    }

    @Override
    public void updateApplicationVersionState(String name, String version, ApplicationState state) {
        ApplicationBase appBase = findByName(name);
        appBase.getVersions().stream()
                .filter(appVersion -> appVersion.getVersion().equals(version))
                .findAny()
                .ifPresent(appVersion -> appVersion.setState(state));
        appBase.validate();
        appBaseRepository.save(appBase);
    }

    @Override
    public List<ApplicationBase> findAll() {
        return appBaseRepository.findAll();
    }

    @Override
    public List<ApplicationBase> findAllActiveOrDisabledApps() {
        return appBaseRepository.findAll().stream()
                .filter(this::isAppActiveOrDisabled)
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationBase getBaseApp(Long id) {
        return appBaseRepository.findById(id).orElseThrow(() -> new MissingElementException("App cannot be found"));
    }

    @Override
    public boolean isAppActive(ApplicationBase application) {
        return application.getVersions().stream()
                .anyMatch(app -> app.getState().equals(ApplicationState.ACTIVE));
    }

    private boolean isAppActiveOrDisabled(ApplicationBase applicationBase){
        return applicationBase.getVersions().stream()
                .anyMatch(app -> Arrays.asList(ApplicationState.ACTIVE, ApplicationState.DISABLED).contains(app.getState()));
    }

    @Override
    public ApplicationBase findByName(String name){
        return appBaseRepository.findByName(name).orElseThrow(() -> new MissingElementException(name + " app base not found"));
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
