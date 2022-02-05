package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationStatePerDomain;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.ApplicationInstanceUpgradeService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationStatePerDomainService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationInstanceServiceTest {

    AppInstanceRepository appInstanceRepo = mock(AppInstanceRepository.class);
    ApplicationService applications = mock(ApplicationService.class);
    DomainService domains = mock(DomainService.class);
    UserService users = mock(UserService.class);
    ApplicationSubscriptionService applicationSubscriptions = mock(ApplicationSubscriptionService.class);
    DomainServiceImpl.CodenameValidator validator = mock(DomainServiceImpl.CodenameValidator.class);
    ApplicationStatePerDomainService applicationStatePerDomainService = mock(ApplicationStatePerDomainService.class);
    ApplicationInstanceUpgradeService applicationInstanceUpgradeService = mock(ApplicationInstanceUpgradeService.class);

    ApplicationInstanceServiceImpl applicationInstanceService = new ApplicationInstanceServiceImpl(
                appInstanceRepo,
                applications,
                domains,
                users,
                applicationSubscriptions,
                validator,
                applicationStatePerDomainService,
                applicationInstanceUpgradeService);


    @Test
    public void createByIdsMethodShouldThrowObjectNotFoundExceptionDueToApplicationObjectDoNotExists(){
        assertThrows(ObjectNotFoundException.class, () -> {
            when(applications.findApplication(anyLong())).thenReturn(Optional.empty());
            applicationInstanceService.create((long) 0, (long) 0, "test", true);
        });
    }

    @Test
    public void createByIdsMethodShouldThrowObjectNotFoundExceptionDueToDomainObjectDoNotExists(){
        assertThrows(ObjectNotFoundException.class, () -> {
            Application app = new Application("test", "testVersion");
            when(applications.findApplication(anyLong())).thenReturn(Optional.of(app));
            when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
            applicationInstanceService.create((long) 0, (long) 0, "test", true);
        });
    }

    @Test
    public void createMethodShouldThrowIllegalArgumentExceptionDueToDomainIsNull(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = new Application((long) 1, "test", "testVersion");
            applicationInstanceService.create(null, app, "test", true);
        });
    }

    @Test
    public void createMethodShouldThrowIllegalArgumentExceptionDueToDomainIdIsNull(){
        assertThrows(IllegalArgumentException.class, () -> {
            Application app = new Application((long) 1, "test", "testVersion");
            Domain domain = new Domain("test", "test");
            applicationInstanceService.create(domain, app, "test", true);
        });
    }

    @Test
    public void createMethodShouldThrowIllegalArgumentExceptionDueToApplicationIsNull(){
        assertThrows(IllegalArgumentException.class, () -> {
            Domain domain = new Domain((long) 1, "test", "test");
            applicationInstanceService.create(domain, null, "test", true);
        });
    }

    @Test
    public void createMethodShouldThrowIllegalArgumentExceptionDueToApplicationIdIsNull(){
        assertThrows(IllegalArgumentException.class, () -> {
            Domain domain = new Domain((long) 1, "test", "test");
            Application application = new Application("test", "testVersion");
            applicationInstanceService.create(domain, application, "test", true);
        });
    }

    @Test
    public void createMethodShouldThrowIllegalArgumentExceptionDueToNameIsInvalid(){
        assertThrows(IllegalArgumentException.class, () -> {
            Domain domain = new Domain((long) 1, "test", "test");
            Application application = new Application((long) 1, "test", "testVersion");
            when(validator.valid(anyString())).thenReturn(false);
            applicationInstanceService.create(domain, application, "test", true);
        });
    }

    @Test
    public void createMethodShouldThrowApplicationSubscriptionNotActiveExceptionDueToMissingSubscriptionOrSubscriptionNotActive(){
        assertThrows(ApplicationSubscriptionNotActiveException.class, () -> {
            Domain domain = new Domain((long) 1, "test", "test");
            domain.setApplicationStatePerDomain(new ArrayList<>());
            Application application = new Application((long) 1, "test", "testVersion");
            when(validator.valid(anyString())).thenReturn(true);
            when(applicationSubscriptions.isActive(anyString(), isA(Domain.class))).thenReturn(false);
            when(applicationStatePerDomainService.isApplicationEnabledInDomain(domain, application)).thenReturn(true);
            applicationInstanceService.create(domain, application, "test", true);
        });
    }

    @Test
    public void createMethodShouldThrowIllegalArgumentExceptionDueToApplicationDisabledInDomain() {
        assertThrows(IllegalArgumentException.class, () -> {
            ApplicationBase applicationBase = new ApplicationBase(1L, "test");
            ApplicationStatePerDomain appState = new ApplicationStatePerDomain(applicationBase, false);
            Domain domain = new Domain(1L, "test-domain", "test-domain");
            List<ApplicationStatePerDomain> appStateList = new ArrayList<>();
            appStateList.add(appState);
            domain.setApplicationStatePerDomain(appStateList);
            Application application = new Application((long) 1, "test", "testVersion");
            when(validator.valid(anyString())).thenReturn(true);
            applicationInstanceService.create(domain, application, "test", true);
        });
    }

    @Test
    public void createMethodShouldCorrectlyReturnAppInstanceObject(){
        Domain domain = new Domain((long) 1, "test", "test");
        domain.setApplicationStatePerDomain(new ArrayList<>());
        Application application = new Application((long) 1,"test","testversion");
        when(validator.valid(anyString())).thenReturn(true);
        when(applicationSubscriptions.isActive(anyString(), isA(Domain.class))).thenReturn(true);
        AppInstance appInstance = new AppInstance(application, domain, "test", true);
        when(appInstanceRepo.save(isA(AppInstance.class))).thenReturn(appInstance);
        when(applicationStatePerDomainService.isApplicationEnabledInDomain(domain, application)).thenReturn(true);
        AppInstance appInstanceResult = applicationInstanceService.create(domain, application, "test", true);
        assertNotNull(appInstanceResult);
    }

    @Test
    public void createByIdsMethodShouldCorrectlyReturnAppInstanceObject(){
        Domain domain = new Domain((long) 0, "test", "test");
        Application application = new Application((long) 0,"test","testversion");
        when(applications.findApplication(anyLong())).thenReturn(Optional.of(application));
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain));
        when(validator.valid(anyString())).thenReturn(true);
        when(applicationSubscriptions.isActive(anyString(), isA(Domain.class))).thenReturn(true);
        AppInstance appInstance = new AppInstance(application, domain, "test", true);
        when(appInstanceRepo.save(isA(AppInstance.class))).thenReturn(appInstance);
        when(applicationStatePerDomainService.isApplicationEnabledInDomain(domain, application)).thenReturn(true);
        AppInstance appInstanceResult = applicationInstanceService.create((long)0, (long)0, "test", true);
        assertNotNull(appInstanceResult);
    }

    @Test
    public void deleteMethodShouldThrowIllegalArgumentExceptionDueToInvalidAppInstanceId(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.delete(null);
        });
    }

    @Test
    public void deleteMethodShouldNotTriggerDeleteIfInstanceNotFound(){
        when(appInstanceRepo.findById(1L)).thenReturn(Optional.empty());
        applicationInstanceService.delete(1L);
        verify(appInstanceRepo, times(0)).delete(any());
    }

    @Test
    public void deleteMethodShouldSuccessfulDeleteObject(){
        Domain domain = new Domain((long) 0, "test", "test");
        Application application = new Application((long) 0,"test","testversion");
        AppInstance appInstance = new AppInstance(application, domain, "test", true);
        appInstance.setId((long) 0);
        when(appInstanceRepo.findById(anyLong())).thenReturn(Optional.of(appInstance));
        applicationInstanceService.delete((long)0);
        verify(appInstanceRepo).delete(isA(AppInstance.class));
    }

    @Test
    public void updateMethodShouldThrowIllegalArgumentExceptionDueToNullAsApplicationInstance(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.update(null);
        });
    }

    @Test
    public void updateMethodShouldThrowIllegalArgumentExceptionDueToMissingApplicationInstanceId(){
        assertThrows(IllegalArgumentException.class, () -> {
            Domain domain = new Domain((long) 0, "test", "test");
            Application application = new Application((long) 0, "test", "testVersion");
            AppInstance appInstance = new AppInstance(application, domain, "test", true);
            applicationInstanceService.update(appInstance);
        });
    }

    @Test
    public void updateMethodShouldSuccessfulUpdateApplicationInstance(){
        Domain domain = new Domain((long) 0, "test", "test");
        Application application = new Application((long) 0,"test","testversion");
        AppInstance appInstance = new AppInstance(application, domain, "test", true);
        appInstance.setId((long) 0);
        applicationInstanceService.update(appInstance);
        verify(appInstanceRepo).save(isA(AppInstance.class));
    }

    @Test
    public void updateApplicationMethodShouldThrowIllegalArgumentExceptionDueToMissingApplicationInstanceId() {
        Application newApplication = new Application(40L, "test", "newtestversion");
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.updateApplication(null, newApplication);
        });
    }

    @Test
    public void updateApplicationMethodShouldThrowIllegalArgumentExceptionDueToMissingApplication() {
        Domain domain = new Domain(10L, "test", "test");
        Application application = new Application(20L,"test","testversion");
        AppInstance appInstance = new AppInstance(application, domain, "test", true);
        appInstance.setId(30L);
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.updateApplication(appInstance, null);
        });
    }

    @Test
    public void updateApplicationMethodShouldSuccessfulUpdateApplicationInstance() {
        Domain domain = new Domain(10L, "test", "test");
        Application application = new Application(20L,"test","testversion");
        AppInstance appInstance = new AppInstance(application, domain, "test", true);
        appInstance.setId(30L);
        Application newApplication = new Application(40L, "test", "newtestversion");
        applicationInstanceService.updateApplication(appInstance, newApplication);

        ArgumentCaptor<AppInstance> appInstanceArgumentCaptor = ArgumentCaptor.forClass(AppInstance.class);
        verify(appInstanceRepo).save(appInstanceArgumentCaptor.capture());
        assertThat(appInstanceArgumentCaptor.getValue().getApplication().getId()).isEqualTo(40L);
        assertThat(appInstanceArgumentCaptor.getValue().getPreviousApplicationId()).isEqualTo(20L);
    }

    @Test
    public void findMethodShouldThrowIllegalArgumentExceptionDueToNullAsApplicationInstanceId(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.find(null);
        });
    }

    @Test
    public void findReturnEmptyOptionalIfInstanceNotFound(){
        when(appInstanceRepo.findById(anyLong())).thenReturn(Optional.empty());
        assertFalse(applicationInstanceService.find(1L).isPresent());
    }

    @Test
    public void findMethodShouldSuccessfulReturnObject(){
        Domain domain = new Domain((long) 0, "test", "test");
        Application application = new Application((long) 0,"test","testversion");
        AppInstance appInstance = new AppInstance(application, domain, "test", true);
        appInstance.setId((long) 0);
        when(appInstanceRepo.findById(anyLong())).thenReturn(Optional.of(appInstance));
        Optional<AppInstance> result = applicationInstanceService.find((long)0);
        assertTrue(result.isPresent());
    }

    @Test
    public void findAllMethodShouldCallAppInstanceRepoFindAll(){
        applicationInstanceService.findAll();
        verify(appInstanceRepo).findAll();
    }

    @Test
    public void findAllByOwnerByUserIdMethodShouldThrowIllegalArgumentExceptionDueToInvalidUserId(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.findAllByOwner((Long) null);
        });
    }

    @Test
    public void findAllByOwnerByUserIdMethodShouldThrowObjectNotFoundExceptionWhenThereIsNoUser(){
        assertThrows(ObjectNotFoundException.class, () -> {
            when(users.findById(anyLong())).thenReturn(Optional.empty());
            applicationInstanceService.findAllByOwner((long) 0);
        });
    }

    @Test
    public void findAllByOwnerByUserIdMethodShouldSuccessfulCallFindAllByOwnerByUserObject(){
        User user = new User("test", true);
        user.setId((long) 0);
        when(users.findById(anyLong())).thenReturn(Optional.of(user));
        applicationInstanceService.findAllByOwner((long) 0);
        verify(appInstanceRepo).findAllByOwner(isA(User.class));
    }

    @Test
    public void findAllByOwnerByUserObjectShouldThrowIllegalArgumentExceptionDueToNullUser(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.findAllByOwner((User) null);
        });
    }

    @Test
    public void findAllByOwnerByUserObjectShouldThrowIllegalArgumentExceptionDueToInvalidId(){
        assertThrows(IllegalArgumentException.class, () -> {
            User user = new User("test", true);
            applicationInstanceService.findAllByOwner(user);
        });
    }

    @Test
    public void findAllByOwnerByUserObjectShouldSuccessfulCallFindAllByOwnerFromAppInstanceRepo(){
        User user = new User("test", true);
        user.setId((long) 0);
        applicationInstanceService.findAllByOwner(user);
        verify(appInstanceRepo).findAllByOwner(isA(User.class));
    }

    @Test
    public void findAllByDomainByIdShouldThrowIllegalArgumentExceptionDueToInvalidIdOfDomain(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.findAllByDomain((Long) null);
        });
    }

    @Test
    public void findAllByDomainByIdShouldThrowObjectNotFoundExceptionExceptionDueToDomainNotExist(){
        assertThrows(ObjectNotFoundException.class, () -> {
            when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
            applicationInstanceService.findAllByDomain((long) 0);
        });
    }

    @Test
    public void findAllByDomainByIdShouldSuccessfulCallFindAllDomainByDomainObject(){
        Domain domain = new Domain((long) 0, "test", "test");
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain));
        applicationInstanceService.findAllByDomain((long)0);
        verify(appInstanceRepo).findAllByDomain(isA(Domain.class));
    }

    @Test
    public void findAllByDomainByDomainObjectShouldThrowIllegalArgumentExceptionDueToNullAsDomain(){
        assertThrows(IllegalArgumentException.class, () -> applicationInstanceService.findAllByDomain((Domain) null));
    }

    @Test
    public void findAllByDomainByDomainObjectShouldThrowIllegalArgumentExceptionDueToMissingIdOfDomain(){
        assertThrows(IllegalArgumentException.class, () -> {
            Domain domain = new Domain("test", "test");
            applicationInstanceService.findAllByDomain(domain);
        });
    }

    @Test
    public void findAllByDomainShouldCallFindAllDomainFromAppInstanceRepo(){
        Domain domain = new Domain((long) 0, "test", "test");
        applicationInstanceService.findAllByDomain(domain);
        verify(appInstanceRepo).findAllByDomain(isA(Domain.class));
    }

    @Test
    public void getDomainShouldThrowIllegalArgumentExceptionDueToMissingDomainId(){
        assertThrows(IllegalArgumentException.class, () -> applicationInstanceService.getDomain(null));
    }

    @Test
    public void getDomainShouldThrowObjectNotFoundExceptionExceptionDueToDomainNotExist(){
        assertThrows(ObjectNotFoundException.class, () -> {
            when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
            applicationInstanceService.getDomain((long) 0);
        });
    }

    @Test
    public void getDomainShouldSuccessfulReturnDomainObject(){
        Domain domain = new Domain((long) 0, "test", "test");
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain));
        Domain result = applicationInstanceService.getDomain((long)0);
        assertNotNull(result);
    }

    @Test
    public void getUserShouldThrowIllegalArgumentExceptionDueToMissingUserId(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.getUser(null);
        });
    }

    @Test
    public void getUserShouldThrowObjectNotFoundExceptionExceptionDueToUserNotExist(){
        assertThrows(ObjectNotFoundException.class, () -> {
            when(users.findById(anyLong())).thenReturn(Optional.empty());
            applicationInstanceService.getUser((long) 0);
        });
    }

    @Test
    public void getUserShouldSuccessfulReturnUserObject(){
        User user = new User("test", true);
        user.setId((long) 0);
        when(users.findById(anyLong())).thenReturn(Optional.of(user));
        User result = applicationInstanceService.getUser((long) 0);
        assertNotNull(result);
    }

    @Test
    public void findAllByOwnerAtDomainShouldThrowIllegalArgumentExceptionDueToMissingUserId(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.findAllByOwner(null, (long) 0);
        });
    }

    @Test
    public void findAllByOwnerAtDomainShouldThrowIllegalArgumentExceptionDueToMissingDomainId(){
        assertThrows(IllegalArgumentException.class, () -> {
            when(users.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));
            applicationInstanceService.findAllByOwner((long) 0, (Long) null);
        });
    }

    @Test
    public void findAllByOwnerAtDomainShouldThrowObjectNotFoundExceptionExceptionDueToMissingDomain(){
        assertThrows(ObjectNotFoundException.class, () -> {
            when(users.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));
            when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
            applicationInstanceService.findAllByOwner((long) 0, (long) 0);
        });
    }

    @Test
    public void findAllByOwnerAtDomainShouldThrowObjectNotFoundExceptionExceptionDueToMissingUser(){
        assertThrows(ObjectNotFoundException.class, () -> {
            when(users.findById(anyLong())).thenReturn(Optional.empty());
            applicationInstanceService.findAllByOwner((long) 0, (long) 0);
        });
    }

    @Test
    public void findAllByOwnerAtDomainShouldSuccessfulCallFindAllByOwnerAtDomainByObjects(){
        when(users.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(mock(Domain.class)));
        applicationInstanceService.findAllByOwner((long) 0,(long) 0);
        verify(appInstanceRepo).findAllByOwnerAndDomain(isA(User.class), isA(Domain.class));
    }

    @Test
    public void findAllByOwnerAtDomainShouldSuccessfulReturnListOfAppInstances(){
        when(users.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(mock(Domain.class)));
        User user = new User("test", true);
        user.setId((long) 0);
        Domain domain = new Domain("test", "test");
        domain.setId((long) 0);
        Application testApp = new Application("test","testversion");
        AppInstance test1 = new AppInstance(testApp, domain, "test1", true);
        List<AppInstance> testList = new ArrayList<>();
        testList.add(test1);
        when(appInstanceRepo.findAllByOwnerAndDomain(isA(User.class), isA(Domain.class))).thenReturn(testList);
        List<AppInstance> resultList = applicationInstanceService.findAllByOwner((long) 0,(long) 0);
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
    }

    @Test
    public void findAllByOwnerAtDomainCalledWithObjectShouldShouldThrowIllegalArgumentExceptionDueToNullAsUser(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInstanceService.findAllByOwnerAndDomain(null, mock(Domain.class));
        });
    }

    @Test
    public void findAllByOwnerAtDomainCalledWithObjectShouldShouldThrowIllegalArgumentExceptionDueToMissingUserId(){
        assertThrows(IllegalArgumentException.class, () -> {
            User user = new User("test", true);
            applicationInstanceService.findAllByOwnerAndDomain(user, mock(Domain.class));
        });
    }

    @Test
    public void findAllByOwnerAtDomainCalledWithObjectShouldShouldThrowIllegalArgumentExceptionDueToNullAsDomain(){
        assertThrows(IllegalArgumentException.class, () -> {
            User user = new User("test", true);
            user.setId((long) 0);
            applicationInstanceService.findAllByOwnerAndDomain(user, null);
        });
    }

    @Test
    public void findAllByOwnerAtDomainCalledWithObjectShouldShouldThrowIllegalArgumentExceptionDueToMissingDomainId(){
        assertThrows(IllegalArgumentException.class, () -> {
            User user = new User("test", true);
            Domain domain = new Domain("test", "test");
            applicationInstanceService.findAllByOwnerAndDomain(user, domain);
        });
    }

    @Test
    public void findAllByOwnerAtDomainCalledWithObjectShouldSuccessfulCallFindAllByOwnerAndDomainFromAppInstanceRepo(){
        User user = new User("test", true);
        user.setId((long) 0);
        Domain domain = new Domain("test", "test");
        domain.setId((long) 0);
        applicationInstanceService.findAllByOwnerAndDomain(user, domain);
        verify(appInstanceRepo).findAllByOwnerAndDomain(isA(User.class), isA(Domain.class));
    }

    @Test
    public void findAllByOwnerAtDomainCalledWithObjectShouldReturnListOfAppInstanceObjects(){
        User user = new User("test", true);
        user.setId((long) 0);
        Domain domain = new Domain("test", "test");
        domain.setId((long) 0);
        Application testApp = new Application("test","testversion");
        AppInstance test1 = new AppInstance(testApp, domain, "test1", true);
        List<AppInstance> testList = new ArrayList<>();
        testList.add(test1);
        when(appInstanceRepo.findAllByOwnerAndDomain(isA(User.class), isA(Domain.class))).thenReturn(testList);
        List<AppInstance> resultList = applicationInstanceService.findAllByOwnerAndDomain(user, domain);
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
    }

    @Test
    public void shouldCheckIfUpgradePossibleAndObtainUpgradeInfo() {
        Domain domain = new Domain("test", "test");
        domain.setId((long) 0);
        Application testApp = new Application(1L, "test","1.1.1");
        KubernetesChart kubernetesChart = new KubernetesChart("testapp", "1.0.0");
        KubernetesTemplate kubernetesTemplate = new KubernetesTemplate(kubernetesChart, null, null);
        testApp.setAppDeploymentSpec(AppDeploymentSpec.builder().kubernetesTemplate(kubernetesTemplate).build());
        Application testApp2 = new Application(2L, "test2","1.2.0");
        KubernetesChart kubernetesChart2 = new KubernetesChart("testapp", "1.1.0");
        KubernetesTemplate kubernetesTemplate2 = new KubernetesTemplate(kubernetesChart2, null, null);
        testApp2.setAppDeploymentSpec(AppDeploymentSpec.builder().kubernetesTemplate(kubernetesTemplate2).build());
        AppInstance test1 = new AppInstance(100L, testApp, domain, "test1", true);
        when(applications.findApplication(any())).thenReturn(Optional.of(testApp2));
        when(appInstanceRepo.findById(100L)).thenReturn(Optional.of(test1));
        when(applicationInstanceUpgradeService.getNextApplicationVersionForUpgrade(any(), any())).thenReturn(Optional.of(2L));

        assertTrue(applicationInstanceService.checkUpgradePossible(test1.getId()));

        AppInstanceView.AppInstanceUpgradeInfo upgradeInfo = applicationInstanceService.obtainUpgradeInfo(test1.getId());
        assertEquals(2L, upgradeInfo.getApplicationId());
        assertEquals("1.2.0", upgradeInfo.getApplicationVersion());
        assertEquals("1.1.0", upgradeInfo.getHelmChartVersion());
    }

}
