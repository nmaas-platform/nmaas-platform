package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.portal.api.domain.ApplicationStatePerDomainView;
import net.geant.nmaas.portal.api.domain.DomainView;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
//@Transactional
public class ApplicationStatePerDomainServiceImpl implements ApplicationStatePerDomainService {

    private DomainRepository domainRepository;
    private ApplicationBaseRepository applicationBaseRepository;

    public static final long DEFAULT_PV_STORAGE_SIZE_LIMIT = 20;

    @Autowired
    ApplicationStatePerDomainServiceImpl(DomainRepository domainRepository, ApplicationBaseRepository applicationBaseRepository){
        this.domainRepository = domainRepository;
        this.applicationBaseRepository = applicationBaseRepository;
    }

    @Override
    public List<ApplicationStatePerDomain> generateListOfDefaultApplicationStatesPerDomain() {

        List<ApplicationStatePerDomain> list = this.applicationBaseRepository.findAll().stream().map(ApplicationStatePerDomain::new).collect(Collectors.toList());
        /*
         * here it is the place to set default state properties, for example default storage limit size
         * NOTE: in this case it's impossible to set `right` value for storage size limit, since we cannot assure that
         * Application object with default properties is available for this Application Base
         */
        list = list.stream().peek(a -> a.setPvStorageSizeLimit(ApplicationStatePerDomainServiceImpl.DEFAULT_PV_STORAGE_SIZE_LIMIT)).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<Domain> updateAllDomainsWithNewApplicationBase(ApplicationBase applicationBase) {
        ApplicationStatePerDomain appState = new ApplicationStatePerDomain(applicationBase);
        appState.setEnabled(true);
        /*
         * same situation as above, setting defaults starts here
         * same situation with storage size limit occurs, but fortunately we have defaults
         * defaults values better not be higher than default limits
         */
        appState.setPvStorageSizeLimit(ApplicationStatePerDomainServiceImpl.DEFAULT_PV_STORAGE_SIZE_LIMIT);
        return domainRepository.saveAll(domainRepository.findAll().stream().peek(domain -> domain.addApplicationState(appState)).collect(Collectors.toList()));
    }

    @Override
    public List<ApplicationStatePerDomain> updateDomain(DomainView changes) {
        Domain updatedDomain = this.domainRepository.getOne(changes.getId());
        List<ApplicationStatePerDomain> list = updatedDomain.getApplicationStatePerDomain();
        for(ApplicationStatePerDomain appState: list){
            for(ApplicationStatePerDomainView appStateView: changes.getApplicationStatePerDomain()){
                if(appState.getApplicationBase().getId().equals(appStateView.getApplicationBaseId())){
                    // rewrite state
                    appState.applyChangedState(appStateView);
                }
            }
        }
        return list;
    }

    @Override
    public boolean isApplicationEnabledInDomain(Domain domain, Application application) {
        Optional<ApplicationBase> appBase = this.applicationBaseRepository.findByName(application.getName());
        if(!appBase.isPresent()){
            throw new IllegalArgumentException("Application name not found");
        }
        return isApplicationEnabledInDomain(domain, appBase.get());
    }

    @Override
    public boolean isApplicationEnabledInDomain(Domain domain, ApplicationBase appBase) {
        for(ApplicationStatePerDomain a: domain.getApplicationStatePerDomain()) {
            if (a.getApplicationBase().getId().equals(appBase.getId())) {
                if(a.isEnabled()){
                    return true;
                }
                break;
            }
        }
        return false;
    }

    // in some cases the storage space may be set at the later stage (if not set by the user)
    @Override
    public boolean validateAppConfigurationAgainstState(AppConfigurationView appConfig, ApplicationStatePerDomain appState) {
        return appConfig.getStorageSpace() == null || appConfig.getStorageSpace() <= appState.getPvStorageSizeLimit();
    }
}
