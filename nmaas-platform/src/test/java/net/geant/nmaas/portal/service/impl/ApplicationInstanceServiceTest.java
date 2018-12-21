package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationInstanceServiceTest {

    @Mock
    AppInstanceRepository appInstanceRepo;

    @Mock
    ApplicationService applications;

    @Mock
    DomainService domains;

    @Mock
    UserService users;

    @Mock
    ApplicationSubscriptionService applicationSubscriptions;

    @Mock
    DomainServiceImpl.CodenameValidator validator;

    @InjectMocks
    ApplicationInstanceServiceImpl applicationInstanceService;

    @Before
    public void setup(){
        applicationInstanceService = new ApplicationInstanceServiceImpl(appInstanceRepo, applications, domains, users, applicationSubscriptions, validator);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void createByIdsMethodShouldThrowObjectNotFoundExceptionDueToApplicationObjectDoNotExists(){
        when(applications.findApplication(anyLong())).thenReturn(Optional.empty());
        AppInstance appInstance = applicationInstanceService.create((long) 0, (long)0, "test");
    }

    @Test(expected = ObjectNotFoundException.class)
    public void createByIdsMethodShouldThrowObjectNotFoundExceptionDueToDomainObjectDoNotExists(){
        Application app = new Application("test");
        when(applications.findApplication(anyLong())).thenReturn(Optional.of(app));
        when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
        AppInstance appInstance = applicationInstanceService.create((long) 0, (long) 0, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToDomainIsNull(){
        Application app = new Application((long) 1, "test");
        AppInstance appInstance = applicationInstanceService.create(null, app, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToDomainIdIsNull(){
        Application app = new Application((long) 1, "test");
        Domain domain = new Domain("test", "test");
        AppInstance appInstance = applicationInstanceService.create(domain, app, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToApplicationIsNull(){
        Domain domain = new Domain((long) 1, "test", "test");
        AppInstance appInstance = applicationInstanceService.create(domain, null, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToApplicationIdIsNull(){
        Domain domain = new Domain((long) 1, "test", "test");
        Application application = new Application("test");
        AppInstance appInstance = applicationInstanceService.create(domain, application, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToNameIsInvalid(){
        Domain domain = new Domain((long) 1, "test", "test");
        Application application = new Application((long) 1,"test");
        when(validator.valid(anyString())).thenReturn(false);
        AppInstance appInstance = applicationInstanceService.create(domain, application, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToNameIsNotUnique(){
        Domain domain = new Domain((long) 1, "test", "test");
        Application application = new Application((long) 1,"test");
        when(validator.valid(anyString())).thenReturn(true);
        List<AppInstance> appInstances = new ArrayList<>();
        AppInstance appInstance = new AppInstance(application, domain, "test");
        appInstances.add(appInstance);
        when(appInstanceRepo.findAllByDomain(isA(Domain.class))).thenReturn(appInstances);
        AppInstance appInstanceResult = applicationInstanceService.create(domain, application, "test");
    }

    @Test(expected = ApplicationSubscriptionNotActiveException.class)
    public void createMethodShouldThrowApplicationSubscriptionNotActiveExceptionDueToMissingSubscriptionOrSubscriptionNotActive(){
        Domain domain = new Domain((long) 1, "test", "test");
        Application application = new Application((long) 1,"test");
        when(validator.valid(anyString())).thenReturn(true);
        List<AppInstance> appInstances = new ArrayList<>();
        when(appInstanceRepo.findAllByDomain(isA(Domain.class))).thenReturn(appInstances);
        when(applicationSubscriptions.isActive(isA(Application.class), isA(Domain.class))).thenReturn(false);
        AppInstance appInstanceResult = applicationInstanceService.create(domain, application, "test");
    }

    @Test
    public void createMethodShouldCorrectlyReturnAppInstanceObject(){
        Domain domain = new Domain((long) 1, "test", "test");
        Application application = new Application((long) 1,"test");
        when(validator.valid(anyString())).thenReturn(true);
        List<AppInstance> appInstances = new ArrayList<>();
        when(appInstanceRepo.findAllByDomain(isA(Domain.class))).thenReturn(appInstances);
        when(applicationSubscriptions.isActive(isA(Application.class), isA(Domain.class))).thenReturn(true);
        AppInstance appInstance = new AppInstance(application, domain, "test");
        when(appInstanceRepo.save(isA(AppInstance.class))).thenReturn(appInstance);
        AppInstance appInstanceResult = applicationInstanceService.create(domain, application, "test");
        assertNotNull(appInstanceResult);
    }

    @Test
    public void createByIdsMethodShouldCorrectlyReturnAppInstanceObject(){
        Domain domain = new Domain((long) 0, "test", "test");
        Application application = new Application((long) 0,"test");
        when(applications.findApplication(anyLong())).thenReturn(Optional.of(application));
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain));
        when(validator.valid(anyString())).thenReturn(true);
        List<AppInstance> appInstances = new ArrayList<>();
        when(appInstanceRepo.findAllByDomain(isA(Domain.class))).thenReturn(appInstances);
        when(applicationSubscriptions.isActive(isA(Application.class), isA(Domain.class))).thenReturn(true);
        AppInstance appInstance = new AppInstance(application, domain, "test");
        when(appInstanceRepo.save(isA(AppInstance.class))).thenReturn(appInstance);
        AppInstance appInstanceResult = applicationInstanceService.create((long)0, (long)0, "test");
        assertNotNull(appInstanceResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteMethodShouldThrowIllegalArgumentExceptionDueToInvalidAppInstanceId(){
        applicationInstanceService.delete(null);
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
        Application application = new Application((long) 0,"test");
        AppInstance appInstance = new AppInstance(application, domain, "test");
        appInstance.setId((long) 0);
        when(appInstanceRepo.findById(anyLong())).thenReturn(Optional.of(appInstance));
        applicationInstanceService.delete((long)0);
        verify(appInstanceRepo).delete(isA(AppInstance.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowIllegalArgumentExceptionDueToNullAsApplicationInstance(){
        applicationInstanceService.update(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowIllegalArgumentExceptionDueToMissingApplicationInstanceId(){
        Domain domain = new Domain((long) 0, "test", "test");
        Application application = new Application((long) 0,"test");
        AppInstance appInstance = new AppInstance(application, domain, "test");
        applicationInstanceService.update(appInstance);
    }

    @Test
    public void updateMethodShouldSuccessfulUpdateApplicationInstance(){
        Domain domain = new Domain((long) 0, "test", "test");
        Application application = new Application((long) 0,"test");
        AppInstance appInstance = new AppInstance(application, domain, "test");
        appInstance.setId((long) 0);
        applicationInstanceService.update(appInstance);
        verify(appInstanceRepo).save(isA(AppInstance.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findMethodShouldThrowIllegalArgumentExceptionDueToNullAsApplicationInstanceId(){
        applicationInstanceService.find(null);
    }

    @Test
    public void findReturnEmptyOptionalIfInstanceNotFound(){
        when(appInstanceRepo.findById(anyLong())).thenReturn(Optional.empty());
        assertFalse(applicationInstanceService.find(1L).isPresent());
    }

    @Test
    public void findMethodShouldSuccessfulReturnObject(){
        Domain domain = new Domain((long) 0, "test", "test");
        Application application = new Application((long) 0,"test");
        AppInstance appInstance = new AppInstance(application, domain, "test");
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

    @Test(expected = IllegalArgumentException.class)
    public void findAllByOwnerByUserIdMethodShouldThrowIllegalArgumentExceptionDueToInvalidUserId(){
        applicationInstanceService.findAllByOwner((Long) null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void findAllByOwnerByUserIdMethodShouldThrowObjectNotFoundExceptionWhenThereIsNoUser(){
        when(users.findById(anyLong())).thenReturn(Optional.empty());
        applicationInstanceService.findAllByOwner((long) 0);
    }

    @Test
    public void findAllByOwnerByUserIdMethodShouldSuccessfulCallFindAllByOwnerByUserObject(){
        User user = new User("test", true);
        user.setId((long) 0);
        when(users.findById(anyLong())).thenReturn(Optional.of(user));
        applicationInstanceService.findAllByOwner((long) 0);
        verify(appInstanceRepo).findAllByOwner(isA(User.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByOwnerByUserObjectShouldThrowIllegalArgumentExceptionDueToNullUser(){
        applicationInstanceService.findAllByOwner((User) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByOwnerByUserObjectShouldThrowIllegalArgumentExceptionDueToInvalidId(){
        User user = new User("test", true);
        applicationInstanceService.findAllByOwner(user);
    }

    @Test
    public void findAllByOwnerByUserObjectShouldSuccessfulCallFindAllByOwnerFromAppInstanceRepo(){
        User user = new User("test", true);
        user.setId((long) 0);
        applicationInstanceService.findAllByOwner(user);
        verify(appInstanceRepo).findAllByOwner(isA(User.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByDomainByIdShouldThrowIllegalArgumentExceptionDueToInvalidIdOfDomain(){
        applicationInstanceService.findAllByDomain((Long) null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void findAllByDomainByIdShouldThrowObjectNotFoundExceptionExceptionDueToDomainNotExist(){
        when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
        applicationInstanceService.findAllByDomain((long) 0);
    }

    @Test
    public void findAllByDomainByIdShouldSuccessfulCallFindAllDomainByDomainObject(){
        Domain domain = new Domain((long) 0, "test", "test");
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain));
        applicationInstanceService.findAllByDomain((long)0);
        verify(appInstanceRepo).findAllByDomain(isA(Domain.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByDomainByDomainObjectShouldThrowIllegalArgumentExceptionDueToNullAsDomain(){
        applicationInstanceService.findAllByDomain((Domain)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByDomainByDomainObjectShouldThrowIllegalArgumentExceptionDueToMissingIdOfDomain(){
        Domain domain = new Domain("test", "test");
        applicationInstanceService.findAllByDomain(domain);
    }

    @Test
    public void findAllByDomainShouldCallFindAllDomainFromAppInstanceRepo(){
        Domain domain = new Domain((long) 0, "test", "test");
        applicationInstanceService.findAllByDomain(domain);
        verify(appInstanceRepo).findAllByDomain(isA(Domain.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDomainShouldThrowIllegalArgumentExceptionDueToMissingDomainId(){
        applicationInstanceService.getDomain(null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getDomainShouldThrowObjectNotFoundExceptionExceptionDueToDomainNotExist(){
        when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
        applicationInstanceService.getDomain((long)0);
    }

    @Test
    public void getDomainShouldSuccessfulReturnDomainObject(){
        Domain domain = new Domain((long) 0, "test", "test");
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(domain));
        Domain result = applicationInstanceService.getDomain((long)0);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getUserShouldThrowIllegalArgumentExceptionDueToMissingUserId(){
        applicationInstanceService.getUser(null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void getUserShouldThrowObjectNotFoundExceptionExceptionDueToUserNotExist(){
        when(users.findById(anyLong())).thenReturn(Optional.empty());
        applicationInstanceService.getUser((long)0);
    }

    @Test
    public void getUserShouldSuccessfulReturnUserObject(){
        User user = new User("test", true);
        user.setId((long) 0);
        when(users.findById(anyLong())).thenReturn(Optional.of(user));
        User result = applicationInstanceService.getUser((long) 0);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByOwnerAtDomainShouldThrowIllegalArgumentExceptionDueToMissingUserId(){
        applicationInstanceService.findAllByOwner((Long) null, (long)0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByOwnerAtDomainShouldThrowIllegalArgumentExceptionDueToMissingDomainId(){
        when(users.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));
        applicationInstanceService.findAllByOwner((long)0, (Long) null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void findAllByOwnerAtDomainShouldThrowObjectNotFoundExceptionExceptionDueToMissingDomain(){
        when(users.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));
        when(domains.findDomain(anyLong())).thenReturn(Optional.empty());
        applicationInstanceService.findAllByOwner((long)0,(long)0);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void findAllByOwnerAtDomainShouldThrowObjectNotFoundExceptionExceptionDueToMissingUser(){
        when(users.findById(anyLong())).thenReturn(Optional.empty());
        applicationInstanceService.findAllByOwner((long)0,(long)0);
    }

    @Test
    public void findAllByOwnerAtDomainShouldSuccessfulCallFindAllByOwnerAtDomainByObjects(){
        when(users.findById(anyLong())).thenReturn(Optional.of(mock(User.class)));
        when(domains.findDomain(anyLong())).thenReturn(Optional.of(mock(Domain.class)));
        applicationInstanceService.findAllByOwner((long)0,(long)0);
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
        Application testApp = new Application("test");
        AppInstance test1 = new AppInstance(testApp, domain, "test1");
        List<AppInstance> testList = new ArrayList<>();
        testList.add(test1);
        when(appInstanceRepo.findAllByOwnerAndDomain(isA(User.class), isA(Domain.class))).thenReturn(testList);
        List<AppInstance> resultList = applicationInstanceService.findAllByOwner((long)0,(long)0);
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByOwnerAtDomainCalledWithObjectShouldShouldThrowIllegalArgumentExceptionDueToNullAsUser(){
        applicationInstanceService.findAllByOwner((User) null, mock(Domain.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByOwnerAtDomainCalledWithObjectShouldShouldThrowIllegalArgumentExceptionDueToMissingUserId(){
        User user = new User("test", true);
        applicationInstanceService.findAllByOwner(user, mock(Domain.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByOwnerAtDomainCalledWithObjectShouldShouldThrowIllegalArgumentExceptionDueToNullAsDomain(){
        User user = new User("test", true);
        user.setId((long) 0);
        applicationInstanceService.findAllByOwner(user, (Domain) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllByOwnerAtDomainCalledWithObjectShouldShouldThrowIllegalArgumentExceptionDueToMissingDomainId(){
        User user = new User("test", true);
        Domain domain = new Domain("test", "test");
        applicationInstanceService.findAllByOwner(user, domain);
    }

    @Test
    public void findAllByOwnerAtDomainCalledWithObjectShouldSuccessfulCallFindAllByOwnerAndDomainFromAppInstanceRepo(){
        User user = new User("test", true);
        user.setId((long) 0);
        Domain domain = new Domain("test", "test");
        domain.setId((long) 0);
        applicationInstanceService.findAllByOwner(user, domain);
        verify(appInstanceRepo).findAllByOwnerAndDomain(isA(User.class), isA(Domain.class));
    }

    @Test
    public void findAllByOwnerAtDomainCalledWithObjectShouldReturnListOfAppInstanceObjects(){
        User user = new User("test", true);
        user.setId((long) 0);
        Domain domain = new Domain("test", "test");
        domain.setId((long) 0);
        Application testApp = new Application("test");
        AppInstance test1 = new AppInstance(testApp, domain, "test1");
        List<AppInstance> testList = new ArrayList<>();
        testList.add(test1);
        when(appInstanceRepo.findAllByOwnerAndDomain(isA(User.class), isA(Domain.class))).thenReturn(testList);
        List<AppInstance> resultList = applicationInstanceService.findAllByOwner(user, domain);
        assertNotNull(resultList);
        assertEquals(1, resultList.size());
    }
}
