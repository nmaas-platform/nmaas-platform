package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApplicationStatePerDomainServiceImplTest {

    private DomainRepository domains = mock(DomainRepository.class);
    private ApplicationBaseRepository applications = mock(ApplicationBaseRepository.class);

    private ApplicationStatePerDomainServiceImpl appState;

    private Domain domain1 = mock(Domain.class);
    private ApplicationBase app1 = mock(ApplicationBase.class);

    @BeforeEach
    public void setup() {
        appState = new ApplicationStatePerDomainServiceImpl(domains, applications);
    }

    @Test
    public void shouldGenerateListOfDefaultApplicationStatesWithDefaults() {
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
    public void shouldUpdateAllDomainWithNewApplicationBase() {
        when(domains.findAll()).thenReturn(new ArrayList<Domain>() {{
            add(domain1);
        }});

        List<Domain> result = appState.updateAllDomainsWithNewApplicationBase(app1);

        verify(domains, times(1)).findAll();
        verify(domains, times(1)).saveAll(any());
        verify(domain1, times(1)).addApplicationState(any(ApplicationStatePerDomain.class));

    }

    @Test
    public void shouldReturnTrueIfApplicationIsEnabledInDomain() {
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
    public void shouldReturnFalseIfApplicationIsEnabledInDomain() {
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
    public void shouldThrowExceptionWhereThereIsNoAppBaseMatchingApplication() {
        String mockName="test";
        Application app = mock(Application.class);
        when(app.getName()).thenReturn(mockName);
        when(applications.findByName(mockName)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            appState.isApplicationEnabledInDomain(domain1, app);
        });

        assertEquals("Application name not found", thrown.getMessage());
    }


}
