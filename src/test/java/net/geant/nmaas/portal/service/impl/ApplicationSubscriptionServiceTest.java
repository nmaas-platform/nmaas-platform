package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableSet;
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
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationSubscriptionServiceTest {

    private static final Long APPLICATION_ID = 1L;
    private static final Long DOMAIN_ID = 1L;

    private final ApplicationSubscriptionRepository appSubRepo = mock(ApplicationSubscriptionRepository.class);
    private final DomainService domains = mock(DomainService.class);
    private final ApplicationBaseService applications = mock(ApplicationBaseService.class);
    private final ApplicationStatePerDomainService appStates = mock(ApplicationStatePerDomainService.class);

    private ApplicationSubscriptionService appSubSrv;

    private ApplicationBase app1;
    private Domain domain1;

    @BeforeEach
    void setup() {
        appSubSrv = new ApplicationSubscriptionServiceImpl(appSubRepo, domains, applications, appStates);
        String applicationName = "app1";
        app1 = new ApplicationBase(APPLICATION_ID, applicationName);
        app1.setVersions(ImmutableSet.of(new ApplicationVersion("1.1", ApplicationState.ACTIVE, APPLICATION_ID)));
        String domainName = "DOMAIN1";
        domain1 = new Domain(DOMAIN_ID, domainName, domainName);
        domain1.setApplicationStatePerDomain(new ArrayList<>());
        when(appStates.isApplicationEnabledInDomain(domain1, app1)).thenReturn(true);
    }

    @Test
    void shouldReturnInactive() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1, false);
        when(appSubRepo.findById(applicationSubscription.getId())).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplicationId(DOMAIN_ID, APPLICATION_ID)).thenReturn(Optional.of(applicationSubscription));
        when(applications.findByName(app1.getName())).thenReturn(app1);
        when(appSubRepo.findByDomainAndApplication(domain1, app1)).thenReturn(Optional.of(applicationSubscription));

        assertThat("Subscription should be inactive!",
                !(appSubSrv.isActive(applicationSubscription.getId()) ||
                        appSubSrv.isActive(APPLICATION_ID, DOMAIN_ID) ||
                        appSubSrv.isActive(app1.getName(), domain1)));
    }

    @Test
    void shouldReturnActive() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1, true);
        when(appSubRepo.findById(applicationSubscription.getId())).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplicationId(DOMAIN_ID, APPLICATION_ID)).thenReturn(Optional.of(applicationSubscription));
        when(applications.findByName(app1.getName())).thenReturn(app1);
        when(appSubRepo.findByDomainAndApplication(domain1, app1)).thenReturn(Optional.of(applicationSubscription));

        assertThat("Subscription should be active!",
                appSubSrv.isActive(applicationSubscription.getId()) &&
                        appSubSrv.isActive(APPLICATION_ID, DOMAIN_ID) &&
                        appSubSrv.isActive(app1.getName(), domain1));
    }

    @Test
    void shouldReturnExist() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.existsById(applicationSubscription.getId())).thenReturn(true);
        when(appSubRepo.existsByDomainAndApplicationId(DOMAIN_ID, APPLICATION_ID)).thenReturn(true);
        when(appSubRepo.existsByDomainAndApplication(domain1, app1)).thenReturn(true);

        assertThat("Subscription should exist",
                appSubSrv.existsSubscription(applicationSubscription.getId()) &&
                        appSubSrv.existsSubscription(APPLICATION_ID, DOMAIN_ID) &&
                        appSubSrv.existsSubscription(app1, domain1));
    }

    @Test
    void shouldReturnNotExist() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.existsByDomainAndApplication(any(), any())).thenReturn(false);
        when(appSubRepo.existsByDomainAndApplicationId(any(), any())).thenReturn(false);

        assertThat("Subscription should not exist",
                !(appSubSrv.existsSubscription(applicationSubscription.getId()) ||
                        appSubSrv.existsSubscription(APPLICATION_ID, DOMAIN_ID) ||
                        appSubSrv.existsSubscription(app1, domain1)));
    }

    @Test
    void shouldReturnSubscription() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.findById(applicationSubscription.getId())).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplicationId(DOMAIN_ID, APPLICATION_ID)).thenReturn(Optional.of(applicationSubscription));
        when(appSubRepo.findByDomainAndApplication(domain1, app1)).thenReturn(Optional.of(applicationSubscription));

        assertThat("Search should return object",
                appSubSrv.getSubscription(applicationSubscription.getId()).equals(Optional.of(applicationSubscription)) &&
                        appSubSrv.getSubscription(APPLICATION_ID, DOMAIN_ID).equals(Optional.of(applicationSubscription)) &&
                        appSubSrv.getSubscription(app1, domain1).equals(Optional.of(applicationSubscription)));
    }

    @Test
    void shouldNotReturnSubscription() {
        ApplicationSubscription applicationSubscription = new ApplicationSubscription(domain1, app1);
        when(appSubRepo.findById(any())).thenReturn(Optional.empty());
        when(appSubRepo.findByDomainAndApplicationId(any(), any())).thenReturn(Optional.empty());
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.empty());

        assertThat("Search should return empty optional",
                !appSubSrv.getSubscription(applicationSubscription.getId()).isPresent() &&
                        !appSubSrv.getSubscription(APPLICATION_ID, DOMAIN_ID).isPresent() &&
                        !appSubSrv.getSubscription(app1, domain1).isPresent());
    }

    @Test
    void shouldGetSubscriptionByDomainAndApp(){
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.of(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(domain1, app1);
        assertEquals(1, result.size());
    }

    @Test
    void shouldNotFoundSubscriptionByDomainAndApp(){
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.empty());
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(domain1, app1);
        assertEquals(1, result.size());
        assertNull(result.get(0));
    }

    @Test
    void shouldGetSubscriptionByDomain(){
        when(appSubRepo.findAllByDomain((Domain) any())).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(domain1, null);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetSubscriptionByApp(){
        when(appSubRepo.findAllByApplication((ApplicationBase) any())).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(null, app1);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetAllSubscriptionWithNullsAsArguments(){
        when(appSubRepo.findAll()).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy((Domain) null, (ApplicationBase) null);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetSubscriptions(){
        when(appSubRepo.findAll()).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = this.appSubSrv.getSubscriptions();
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetSubscriptionByDomainIdAndAppId(){
        when(appSubRepo.findByDomainAndApplicationId(anyLong(), anyLong())).thenReturn(Optional.of(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(1L, 1L);
        assertEquals(1, result.size());
    }

    @Test
    void shouldNotFoundSubscriptionByDomainIdAndAppId(){
        when(appSubRepo.findByDomainAndApplicationId(anyLong(), anyLong())).thenReturn(Optional.empty());
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(1L, 1L);
        assertEquals(1, result.size());
        assertNull(result.get(0));
    }

    @Test
    void shouldGetSubscriptionByDomainId(){
        when(appSubRepo.findAllByDomain(anyLong())).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(1L, null);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetSubscriptionByAppId(){
        when(appSubRepo.findAllByApplication(anyLong())).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy(null, 1L);
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetAllSubscriptionWithNullIdsAsArguments(){
        when(appSubRepo.findAll()).thenReturn(Collections.singletonList(new ApplicationSubscription(domain1, app1)));
        List<ApplicationSubscription> result = appSubSrv.getSubscriptionsBy((Long) null, (Long) null);
        assertEquals(1, result.size());
    }

    @Test
    void shouldSubscribeAppFirstTime(){
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        ApplicationSubscription result = this.appSubSrv.subscribe(appSub);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    void shouldSubscribeApp(){
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
    void shouldSubscribeDeletedSubscriptionApp(){
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
    void shouldSubscribeAppFirstTimeWhenEnabledInDomain(){
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        domain1.addApplicationState(app1);
        ApplicationSubscription result = this.appSubSrv.subscribe(appSub);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    void shouldSubscribeDeletedSubscriptionAppWhenEnabledInDomain() {
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        appSub.setDeleted(true);
        domain1.addApplicationState(app1);
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.findById(any())).thenReturn(Optional.of(new ApplicationSubscription(domain1, app1)));
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription result = this.appSubSrv.subscribe(appSub);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    void shouldNotSubscribeAppFirstTimeWhenDisabledInDomain() {
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.save(any())).thenThrow(new IllegalArgumentException());
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        when(appStates.isApplicationEnabledInDomain(domain1, app1)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            this.appSubSrv.subscribe(appSub);
        });

        assertEquals("Cannot subscribe. Application is disabled in this domain", thrown.getMessage());
    }

    @Test
    void shouldNotSubscribeDeletedSubscriptionAppWhenDisabledInDomain() {
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.save(any())).thenThrow(new IllegalArgumentException());
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        appSub.setDeleted(true);
        when(appStates.isApplicationEnabledInDomain(domain1, app1)).thenReturn(false);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            this.appSubSrv.subscribe(appSub);
        });

        assertEquals("Cannot subscribe. Application is disabled in this domain", thrown.getMessage());
    }

    @Test
    void shouldNotSubscribeApp() {
        assertThrows(ProcessingException.class, () -> {
            when(appSubRepo.existsById(any())).thenReturn(false);
            when(appSubRepo.save(any())).thenThrow(new IllegalArgumentException());
            when(applications.isAppActive(app1)).thenReturn(true);
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            this.appSubSrv.subscribe(appSub);
        });
    }

    @Test
    void shouldNotSubscribeAppWithNullAppSub() {
        assertThrows(IllegalArgumentException.class, () -> this.appSubSrv.subscribe(null));
    }

    @Test
    void shouldNotSubscribeAppWithNullAppSubId() {
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            appSub.setId(null);
            this.appSubSrv.subscribe(appSub);
        });
    }

    @Test
    void shouldNotSubscribeAppWithInactiveDomain() {
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            appSub.getDomain().setActive(false);
            this.appSubSrv.subscribe(appSub);
        });
    }

    @Test
    void shouldNotSubscribeAppWithDeletedApp() {
        assertThrows(IllegalStateException.class, () ->{
            when(appSubRepo.existsById(any())).thenReturn(false);
            when(applications.isAppActive(app1)).thenReturn(false);
            ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
            this.appSubSrv.subscribe(appSub);
        });
    }

    @Test
    void shouldSubscribeByAppIdDomainIdFirstTime() {
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
    void shouldSubscribeByAppIdDomainId() {
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
    void shouldNotSubscribeByDomainIdNotFound() {
        assertThrows(ObjectNotFoundException.class, () -> {
            when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
            this.appSubSrv.subscribe(1L, 1L, true);
        });
    }

    @Test
    void shouldNotSubscribeByAppIdNotFound() {
        assertThrows(MissingElementException.class, () -> {
            when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain1));
            when(applications.getBaseApp(anyLong())).thenThrow(new MissingElementException());
            this.appSubSrv.subscribe(1L, 1L, true);
        });
    }

    @Test
    void shouldSubscribeByAppAndDomain() {
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
    void shouldSubscribeByAppAndDomainFirstTime() {
        when(appSubRepo.findByDomainAndApplication(any(), any())).thenReturn(Optional.empty());
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(applications.isAppActive(app1)).thenReturn(true);
        ApplicationSubscription result = this.appSubSrv.subscribe(app1, domain1, true);
        assertTrue(result.isActive());
        assertFalse(result.isDeleted());
    }

    @Test
    void shouldUnsubscribeApp() {
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        appSub.setDeleted(false);
        when(appSubRepo.isDeleted((Domain) any(), any())).thenReturn(false);
        when(appSubRepo.existsById(any())).thenReturn(true);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        this.appSubSrv.unsubscribe(appSub);
        verify(appSubRepo, times(1)).save(any());
    }

    @Test
    void shouldNotUnsubscribeAppWhenIsDeleted() {
        ApplicationSubscription appSub = new ApplicationSubscription(domain1, app1);
        appSub.setDeleted(false);
        when(appSubRepo.isDeleted((Domain) any(), any())).thenReturn(true);
        when(appSubRepo.existsById(any())).thenReturn(false);
        when(appSubRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        this.appSubSrv.unsubscribe(appSub);
        verify(appSubRepo, times(0)).save(any());
    }

    @Test
    void shouldNotUnsubscribeNotExistingApp() {
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
    void shouldNotUnsubscribeWithSaveFailure() {
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
    void shouldUnsubscribeByAppIdAndDomainId() {
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
    void shouldUnsubscribeByAppAndDomain() {
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
    void shouldGetSubscribedApps() {
        when(appSubRepo.findApplicationBriefAllBy()).thenReturn(Collections.singletonList(app1));
        List<ApplicationBase> result = appSubSrv.getSubscribedApplications();
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetSubscribedAppsByDomain() {
        when(appSubRepo.findApplicationBriefAllByDomain(anyLong())).thenReturn(Collections.singletonList(app1));
        List<ApplicationBase> result = appSubSrv.getSubscribedApplications(1L);
        assertEquals(1, result.size());
    }
}
