package net.geant.nmaas.portal.service;

import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.portal.domain.DomainView;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistence.entity.Domain;

import java.util.List;

public interface ApplicationStatePerDomainService {

    List<ApplicationStatePerDomain> generateListOfDefaultApplicationStatesPerDomain();

    List<ApplicationStatePerDomain> generateListOfDefaultApplicationStatesPerDomainDisabled();


    List<Domain> updateAllDomainsWithNewApplicationBase(ApplicationBase applicationBase);

    List<ApplicationStatePerDomain> updateDomain(DomainView changes);

    boolean isApplicationEnabledInDomain(Domain domain, ApplicationBase appBase);

    boolean isApplicationEnabledInDomain(Domain domain, Application application);

    boolean validateAppConfigurationAgainstState(AppConfigurationView appConfig, ApplicationStatePerDomain appState);

}
