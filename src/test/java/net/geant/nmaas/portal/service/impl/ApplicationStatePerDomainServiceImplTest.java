package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.applications.ApplicationStatePerDomainView;
import net.geant.nmaas.api.dto.domains.DomainView;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApplicationStatePerDomainServiceImplTest {

    private final DomainRepository domains = mock(DomainRepository.class);
    private final ApplicationBaseRepository applications = mock(ApplicationBaseRepository.class);

    private final DomainGroupRepository domainGroupRepository = mock(DomainGroupRepository.class);

    private ApplicationStatePerDomainServiceImpl appState;

    private final Domain domain1 = mock(Domain.class);
    private final ApplicationBase app1 = mock(ApplicationBase.class);

    @BeforeEach
    void setup() {
        appState = new ApplicationStatePerDomainServiceImpl(domains, applications, domainGroupRepository);
    }

    @Test
    void shouldGenerateListOfDefaultApplicationStatesWithDefaults() {
        when(applications.findAll()).thenReturn(new ArrayList<ApplicationBase>() {{
            add(app1);
        }});

        List<ApplicationStatePerDomain> result = appState.generateListOfDefaultApplicationStatesPerDomain();

        assertEquals(1, result.size());
        for(ApplicationStatePerDomain a: result){
            assertTrue(a.isEnabled());
            assertEquals(ApplicationStatePerDomainServiceImpl.DEFAULT_PV_STORAGE_SIZE_LIMIT, a.getPvStorageSizeLimit());
        }
    }

    @Test
    void shouldUpdateAllDomainWithNewApplicationBase() {
        DomainGroup group = new DomainGroup(2L, "g", "g");
        when(domains.findAll()).thenReturn(new ArrayList<Domain>() {{
            add(domain1);
        }});
        when(domainGroupRepository.findAll()).thenReturn(List.of(group));

        List<Domain> result = appState.updateAllDomainsWithNewApplicationBase(app1);

        verify(domains, times(1)).findAll();
        verify(domains, times(1)).saveAll(any());
        verify(domain1, times(1)).addApplicationState(any(ApplicationStatePerDomain.class));
        assertEquals(1, group.getApplicationStatePerDomain().size());
        assertFalse(group.getApplicationStatePerDomain().getFirst().isEnabled());
    }

    @Test
    void shouldGenerateListOfDisabledApplicationStatesWithDefaults() {
        when(applications.findAll()).thenReturn(new ArrayList<ApplicationBase>() {{
            add(app1);
        }});

        List<ApplicationStatePerDomain> result = appState.generateListOfDefaultApplicationStatesPerDomainDisabled();

        assertEquals(1, result.size());
        assertFalse(result.getFirst().isEnabled());
        assertEquals(ApplicationStatePerDomainServiceImpl.DEFAULT_PV_STORAGE_SIZE_LIMIT, result.getFirst().getPvStorageSizeLimit());
    }

    @Test
    void shouldReturnTrueIfApplicationIsEnabledInDomain() {
        String mockName="test";
        Application app = mock(Application.class);
        when(app.getName()).thenReturn(mockName);
        when(applications.findByName(mockName)).thenReturn(Optional.of(app1));
        ApplicationStatePerDomain a = mock(ApplicationStatePerDomain.class);
        when(a.getApplicationBase()).thenReturn(app1);
        when(a.isEnabled()).thenReturn(true);
        when(app1.getId()).thenReturn(1L);
        when(domain1.getApplicationStatePerDomain()).thenReturn(new ArrayList<ApplicationStatePerDomain>() {{
            add(a);
        }});

        assertTrue(appState.isApplicationEnabledInDomain(domain1, app));
        assertTrue(appState.isApplicationEnabledInDomain(domain1, app1));
    }

    @Test
    void shouldReturnFalseIfApplicationIsEnabledInDomain() {
        String mockName="test";
        Application app = mock(Application.class);
        when(app.getName()).thenReturn(mockName);
        when(applications.findByName(mockName)).thenReturn(Optional.of(app1));
        ApplicationStatePerDomain a = mock(ApplicationStatePerDomain.class);
        when(a.getApplicationBase()).thenReturn(app1);
        when(a.isEnabled()).thenReturn(false);
        when(app1.getId()).thenReturn(1L);
        when(domain1.getApplicationStatePerDomain()).thenReturn(new ArrayList<ApplicationStatePerDomain>() {{
            add(a);
        }});

        assertFalse(appState.isApplicationEnabledInDomain(domain1, app));
        assertFalse(appState.isApplicationEnabledInDomain(domain1, app1));
    }

    @Test
    void shouldThrowExceptionWhereThereIsNoAppBaseMatchingApplication() {
        String mockName="test";
        Application app = mock(Application.class);
        when(app.getName()).thenReturn(mockName);
        when(applications.findByName(mockName)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            appState.isApplicationEnabledInDomain(domain1, app);
        });

        assertEquals("Application with given name not found", thrown.getMessage());
    }

    @Test
    void updateDomainShouldApplyChangesOnMatchingAppBase() {
        Domain updatedDomain = new Domain(10L, "d", "d");
        ApplicationBase base = new ApplicationBase(5L, "app");
        ApplicationStatePerDomain state = new ApplicationStatePerDomain(base, true, 20);
        updatedDomain.setApplicationStatePerDomain(new ArrayList<>(List.of(state)));

        DomainView changes = mock(DomainView.class);
        ApplicationStatePerDomainView changeView = mock(ApplicationStatePerDomainView.class);
        when(changes.getId()).thenReturn(10L);
        when(changes.getApplicationStatePerDomain()).thenReturn(List.of(changeView));
        when(changeView.getApplicationBaseId()).thenReturn(5L);
        when(changeView.isEnabled()).thenReturn(false);
        when(changeView.getPvStorageSizeLimit()).thenReturn(99L);
        when(domains.getReferenceById(10L)).thenReturn(updatedDomain);

        List<ApplicationStatePerDomain> result = appState.updateDomain(changes);

        assertEquals(1, result.size());
        assertFalse(result.getFirst().isEnabled());
        assertEquals(99L, result.getFirst().getPvStorageSizeLimit());
    }

    @Test
    void validateAppConfigurationAgainstStateShouldReturnTrueForNullStorageAndFalseAboveLimit() {
        AppConfigurationView configNull = new AppConfigurationView();
        AppConfigurationView configOk = new AppConfigurationView();
        configOk.setStorageSpace(10);
        AppConfigurationView configTooBig = new AppConfigurationView();
        configTooBig.setStorageSpace(30);

        ApplicationStatePerDomain state = new ApplicationStatePerDomain(new ApplicationBase(1L, "a"), true, 20);

        assertTrue(appState.validateAppConfigurationAgainstState(configNull, state));
        assertTrue(appState.validateAppConfigurationAgainstState(configOk, state));
        assertFalse(appState.validateAppConfigurationAgainstState(configTooBig, state));
    }

}
