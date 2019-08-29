package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.ApplicationVersion;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.ApplicationSubscriptionRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.DomainService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationSubscriptionServiceTest {

    private ApplicationSubscriptionRepository appSubRepo = mock(ApplicationSubscriptionRepository.class);

    private DomainService domains = mock(DomainService.class);

    private ApplicationBaseService applications = mock(ApplicationBaseService.class);

    private ApplicationSubscriptionService appSubSrv;

    private Long applicationId = 1L;
    private String applicationName = "app1";
    private ApplicationBase app1;

    private Long domainId = 1L;
    private String domainName = "DOMAIN1";
    private Domain domain1;

    @BeforeEach
    public void setup() {
        appSubSrv = new ApplicationSubscriptionServiceImpl(appSubRepo, domains, applications);
        app1 = new ApplicationBase(applicationId, applicationName);
        app1.setVersions(ImmutableSet.of(new ApplicationVersion("1.1", ApplicationState.ACTIVE, applicationId)));
        domain1 = new Domain(domainId, domainName, domainName);
    }

    @Test
    public void shouldReturnInactive() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1, false);
        when(appSubRepo.findById(applicationSubscription.getId())).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplicationId(domainId, applicationId)).thenReturn(Optional.of(applicationSubscription));
        when(applications.findByName(app1.getName())).thenReturn(app1);
        when(appSubRepo.findByDomainAndApplication(domain1, app1)).thenReturn(Optional.of(applicationSubscription));

        assertThat("Subscription should be inactive!",
                !(appSubSrv.isActive(applicationSubscription.getId()) ||
                        appSubSrv.isActive(applicationId, domainId) ||
                        appSubSrv.isActive(app1.getName(), domain1)));
    }

    @Test
    public void shouldReturnActive() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1, true);
        when(appSubRepo.findById(applicationSubscription.getId())).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplicationId(domainId, applicationId)).thenReturn(Optional.of(applicationSubscription));
        when(applications.findByName(app1.getName())).thenReturn(app1);
        when(appSubRepo.findByDomainAndApplication(domain1, app1)).thenReturn(Optional.of(applicationSubscription));

        assertThat("Subscription should be active!",
                appSubSrv.isActive(applicationSubscription.getId()) &&
                        appSubSrv.isActive(applicationId, domainId) &&
                        appSubSrv.isActive(app1.getName(), domain1));
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

    @Test
    public void shouldGetSubscriptionByDomainAndApp(){
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.of(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(domain1, app1);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldNotFoundSubscriptionByDomainAndApp(){
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.empty());
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(domain1, app1);
        assertEquals(1, result.size());
        assertNull(result.get(0));
    }

    @Test
    public void shouldGetSubscriptionByDomain(){
        when(appSubRepo.findAllByDomain((Domain) any())).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(domain1, null);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldGetSubscriptionByApp(){
        when(appSubRepo.findAllByApplication((ApplicationBase) any())).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(null, app1);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldGetAllSubscriptionWithNullsAsArguments(){
        when(appSubRepo.findAll()).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy((Domain) null, (ApplicationBase) null);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldGetSubscriptions(){
        when(appSubRepo.findAll()).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = this.appSubSrv.getSubscriptions();
        assertEquals(1, result.size());
    }

    @Test
    public void shouldGetSubscriptionByDomainIdAndAppId(){
        when(appSubRepo.findByDomainAndApplicationId(anyLong(), anyLong())).thenReturn(Optional.of(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(1L, 1L);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldNotFoundSubscriptionByDomainIdAndAppId(){
        when(appSubRepo.findByDomainAndApplicationId(anyLong(), anyLong())).thenReturn(Optional.empty());
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(1L, 1L);
        assertEquals(1, result.size());
        assertNull(result.get(0));
    }

    @Test
    public void shouldGetSubscriptionByDomainId(){
        when(appSubRepo.findAllByDomain(anyLong())).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(1L, null);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldGetSubscriptionByAppId(){
        when(appSubRepo.findAllByApplication(anyLong())).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(null, 1L);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldGetAllSubscriptionWithNullIdsAsArguments(){
        when(appSubRepo.findAll()).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy((Long) null, (Long) null);
        assertEquals(1, result.size());
    }

    @Test
    public void shouldSubscribeAppFirstTime(){
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        ApplicationSubscription result = this.appSubSrv.subscribe(appSub);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    public void shouldSubscribeApp(){
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.findById(any())).thenReturn(Optional.of(new ApplicationSubscription(domain1, app1)));
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription result = this.appSubSrv.subscribe(appSub);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    public void shouldSubscribeDeletedSubscriptionApp(){
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        appSub.setDeleted(true);
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.findById(any())).thenReturn(Optional.empty());
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription result = this.appSubSrv.subscribe(appSub);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    public void shouldNotSubscribeApp(){
        assertThrows(ProcessingException.class, () -> {
            when(appSubRepo.existsById(any())).thenReturn(false);
            when(appSubRepo.save(any())).thenThrow(new IllegalArgumentException());
            when(applications.isAppActive(app1)).thenReturn(true);
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            this.appSubSrv.subscribe(appSub);
        });
    }

    @Test
    public void shouldNotSubscribeAppWithNullAppSub(){
        assertThrows(IllegalArgumentException.class, () -> this.appSubSrv.subscribe(null));
    }

    @Test
    public void shouldNotSubscribeAppWithNullAppSubId(){
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            appSub.setId(null);
            this.appSubSrv.subscribe(appSub);
        });
    }

    @Test
    public void shouldNotSubscribeAppWithInactiveDomain(){
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            appSub.getDomain().setActive(false);
            this.appSubSrv.subscribe(appSub);
        });
    }

    @Test
    public void shouldNotSubscribeAppWithDeletedApp(){
        assertThrows(IllegalStateException.class, () ->{
            when(appSubRepo.existsById(any())).thenReturn(false);
            when(applications.isAppActive(app1)).thenReturn(false);
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            this.appSubSrv.subscribe(appSub);
        });
    }

    @Test
    public void shouldSubscribeByAppIdDomainIdFirstTime(){
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain1));
        when(applications.getBaseApp(anyLong())).thenReturn(app1);
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.empty());
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.findById(any())).thenReturn(Optional.of(new ApplicationSubscription(domain1, app1)));
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription result = this.appSubSrv.subscribe(1L, 1L, true);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    public void shouldSubscribeByAppIdDomainId(){
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain1));
        when(applications.getBaseApp(anyLong())).thenReturn(app1);
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.of(appSub));
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.findById(any())).thenReturn(Optional.of(appSub));
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription result = this.appSubSrv.subscribe(1L, 1L, true);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    public void shouldNotSubscribeByDomainIdNotFound(){
        assertThrows(ObjectNotFoundException.class, () -> {
            when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
            this.appSubSrv.subscribe(1L, 1L, true);
        });
    }

    @Test
    public void shouldNotSubscribeByAppIdNotFound(){
        assertThrows(MissingElementException.class, () -> {
            when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain1));
            when(applications.getBaseApp(anyLong())).thenThrow(new MissingElementException());
            this.appSubSrv.subscribe(1L, 1L, true);
        });
    }

    @Test
    public void shouldSubscribeByAppAndDomain(){
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.of(appSub));
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.findById(any())).thenReturn(Optional.of(appSub));
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription result = this.appSubSrv.subscribe(app1, domain1, true);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    public void shouldSubscribeByAppAndDomainFirstTime(){
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.empty());
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription result = this.appSubSrv.subscribe(app1, domain1, true);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    public void shouldUnsubscribeApp(){
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        appSub.setDeleted(false);
        when(appSubRepo.isDeleted((Domain) any(), any())).thenReturn(false);
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        this.appSubSrv.unsubscribe(appSub);
        verify(appSubRepo, times(1)).save(any());
    }

    @Test
    public void shouldNotUnsubscribeAppWhenIsDeleted(){
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        appSub.setDeleted(false);
        when(appSubRepo.isDeleted((Domain) any(), any())).thenReturn(true);
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        this.appSubSrv.unsubscribe(appSub);
        verify(appSubRepo, times(0)).save(any());
    }

    @Test
    public void shouldNotUnsubscribeNotExistingApp(){
        assertThrows(ObjectNotFoundException.class, () -> {
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            appSub.setDeleted(false);
            when(appSubRepo.isDeleted((Domain) any(), any())).thenReturn(false);
            when(appSubRepo.existsById(any())).thenReturn(false);
            when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
            this.appSubSrv.unsubscribe(appSub);
        });
    }

    @Test
    public void shouldNotUnsubscribeWithSaveFailure(){
        assertThrows(ProcessingException.class, () -> {
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            appSub.setDeleted(false);
            when(appSubRepo.isDeleted((Domain) any(), any())).thenReturn(false);
            when(appSubRepo.existsById(any())).thenReturn(true);
            when(appSubRepo.save(any())).thenThrow(new IllegalStateException());
            this.appSubSrv.unsubscribe(appSub);
        });
    }

    @Test
    public void shouldUnsubscribeByAppIdAndDomainId(){
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        appSub.setDeleted(false);
        when(appSubRepo.isDeleted((Domain) any(), any())).thenReturn(false);
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(appSubRepo.findByDomainAndApplicationId(any(), any())).thenReturn(Optional.of(appSub));
        this.appSubSrv.unsubscribe(1L, 1L);
        verify(appSubRepo, times(1)).save(any());
    }

    @Test
    public void shouldUnsubscribeByAppAndDomain(){
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        appSub.setDeleted(false);
        when(appSubRepo.isDeleted((Domain) any(), any())).thenReturn(false);
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.of(appSub));
        this.appSubSrv.unsubscribe(app1, domain1);
        verify(appSubRepo, times(1)).save(any());
    }

    @Test
    public void shouldGetSubscribedApps(){
        when(appSubRepo.findApplicationBriefAllBy()).thenReturn(Collections.singletonList(app1));
        List<ApplicationBase> result = appSubSrv.getSubscribedApplications();
        assertEquals(1, result.size());
    }

    @Test
    public void shouldGetSubscribedAppsByDomain(){
        when(appSubRepo.findApplicationBriefAllByDomain(anyLong())).thenReturn(Collections.singletonList(app1));
        List<ApplicationBase> result = appSubSrv.getSubscribedApplications(1L);
        assertEquals(1, result.size());
    }
}
