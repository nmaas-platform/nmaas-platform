package test.java.net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.ApplicationSubscriptionRepository;

import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.impl.ApplicationSubscriptionServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

public class ApplicationSubscriptionServiceTest {

    ApplicationSubscriptionRepository appSubRepo = mock(ApplicationSubscriptionRepository.class);

    DomainService domains = mock(DomainService.class);

    ApplicationService applications = mock(ApplicationService.class);

    ApplicationSubscriptionService appSubSrv;

    Long applicationId = 1L;
    String applicationName = "app1";
    Application app1;

    Long domainId = 1L;
    String domainName = "DOMAIN1";
    Domain domain1;

    @Before
    public void setup() {
        appSubSrv = new ApplicationSubscriptionServiceImpl(appSubRepo, domains, applications);
        app1 = new Application(applicationId, applicationName);
        domain1 = new Domain(domainId, domainName, domainName);
    }

    @Test
    public void shouldReturnInactive() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1, false);
        when(appSubRepo.findById(applicationSubscription.getId())).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplicationId(domainId, applicationId)).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplication(domain1, app1)).thenReturn(Optional.of(applicationSubscription));

        assertThat("Subscription should be inactive!",
                !(appSubSrv.isActive(applicationSubscription.getId()) ||
                        appSubSrv.isActive(applicationId, domainId) ||
                        appSubSrv.isActive(app1, domain1)));
    }

    @Test
    public void shouldReturnActive() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1, true);
        when(appSubRepo.findById(applicationSubscription.getId())).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplicationId(domainId, applicationId)).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplication(domain1, app1)).thenReturn(Optional.of(applicationSubscription));

        assertThat("Subscription should be active!",
                appSubSrv.isActive(applicationSubscription.getId()) &&
                        appSubSrv.isActive(applicationId, domainId) &&
                        appSubSrv.isActive(app1, domain1));
    }

    @Test
    public void shouldReturnExist() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.existsById(applicationSubscription.getId())).thenReturn(true);
        when(appSubRepo.existsByDomainAndApplicationId(domainId, applicationId)).thenReturn(true);
        when(appSubRepo.existsByDomainAndApplication(domain1, app1)).thenReturn(true);

        assertThat("Subscription should exist",
                appSubSrv.existsSubscription(applicationSubscription.getId()) &&
                        appSubSrv.existsSubscription(applicationId, domainId) &&
                        appSubSrv.existsSubscription(app1, domain1));
    }

    @Test
    public void shouldReturnNotExist() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.existsByDomainAndApplication(any(), any())).thenReturn(false);
        when(appSubRepo.existsByDomainAndApplicationId(any(), any())).thenReturn(false);

        assertThat("Subscription should not exist",
                !(appSubSrv.existsSubscription(applicationSubscription.getId()) ||
                        appSubSrv.existsSubscription(applicationId, domainId) ||
                        appSubSrv.existsSubscription(app1, domain1)));
    }

    @Test
    public void shouldReturnSubscription() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.findById(applicationSubscription.getId())).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplicationId(domainId, applicationId)).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplication(domain1, app1)).thenReturn(Optional.of(applicationSubscription));

        assertThat("Search should return object",
                appSubSrv.getSubscription(applicationSubscription.getId()).equals(Optional.of(applicationSubscription)) &&
                        appSubSrv.getSubscription(applicationId, domainId).equals(Optional.of(applicationSubscription)) &&
                        appSubSrv.getSubscription(app1, domain1).equals(Optional.of(applicationSubscription)));
    }

    @Test
    public void shouldNotReturnSubscription() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.findById(any())).thenReturn(Optional.empty());
        when(appSubRepo.findByDomainAndApplicationId(any(), any())).thenReturn(Optional.empty());
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.empty());

        assertThat("Search should return empty optional",
                !appSubSrv.getSubscription(applicationSubscription.getId()).isPresent() &&
                        !appSubSrv.getSubscription(applicationId, domainId).isPresent() &&
                        !appSubSrv.getSubscription(app1, domain1).isPresent());
    }



}
