package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.exceptions.ApplicationSubscriptionNotActiveException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.persistent.entity.projections.ApplicationBriefProjection;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

    //	@Override
    //	public AppInstance create(Long domainId, Long applicationId, String name) throws ObjectNotFoundException, ApplicationSubscriptionNotActiveException {
    //		Application app = applications.findApplication(applicationId).orElseThrow(() -> new ObjectNotFoundException("Application not found."));
    //		Domain domain = domains.findDomain(domainId).orElseThrow(() -> new ObjectNotFoundException("Domain not found."));
    //		return create(domain, app, name);
    //	}
    @Ignore
    @Test(expected = ObjectNotFoundException.class)
    public void createMethodShouldThrowObjectNotFoundExceptionDueToApplicationObjectDoNotExists(){

    }
    @Ignore
    @Test(expected = ObjectNotFoundException.class)
    public void createMethodShouldThrowObjectNotFoundExceptionDueToDomainObjectDoNotExists(){

    }
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToDomainIsNull(){

    }
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToApplicationIsNull(){

    }
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToNameIsNull(){

    }
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowIllegalArgumentExceptionDueToNameIsNotUnique(){

    }
    @Ignore
    @Test(expected = ApplicationSubscriptionNotActiveException.class)
    public void createMethodShouldThrowApplicationSubscriptionNotActiveExceptionDueToMissingSubscriptionOrSubscriptionNotActive(){

    }
    @Ignore
    @Test
    public void createMethodShouldCorrectlyReturnAppInstanceObject(){

    }
    //	@Override
    //	public AppInstance create(Domain domain, Application application, String name) throws ApplicationSubscriptionNotActiveException {
    //		checkParam(domain);
    //		checkParam(application);
    //		checkNameCharacters(name);
    //		checkNameUniqueness(domain, name);
    //		if(applicationSubscriptions.isActive(application, domain))
    //			return appInstanceRepo.save(new AppInstance(application, domain, name));
    //		else
    //			throw new ApplicationSubscriptionNotActiveException("Application subscription is missing or not active.");
    //	}


}
