package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {

    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    ApplicationServiceImpl applicationService;

    @Before
    public void setup(){
        applicationService = new ApplicationServiceImpl(applicationRepository);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowExceptionDueToIncorrectName(){
        applicationService.create(null, null, null);
    }

    @Test
    public void createMethodShouldReturnApplicationObject(){
        Application application = new Application("test","testversion","owner");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        Application result = applicationService.create("test","testversion","owner");
        assertNotNull(result);
        assertEquals("test", result.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToNullPassedAsParameter(){
        applicationService.update(null);
    }

    @Test
    public void updateMethodShouldReturnApplicationObject(){
        Application application = new Application("test", "testversion","owner");
        application.setId(1L);
        application.setLicense("MIT");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        application.setLicense("Apache-2.0");
        Application result = applicationService.update(application);
        assertNotNull(result);
        assertNotEquals("MIT", result.getLicense());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteMethodShouldTrowExceptionDueToNullPassedAsId(){
        applicationService.delete(null);
    }

    @Test
    public void deleteMethodShouldSetApplicationAsDeleted(){
        Application application = new Application("test", "testversion","owner");
        application.setId((long) 0);
        application.setState(ApplicationState.ACTIVE);
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        applicationService.delete((long) 0);
        verify(applicationRepository).findById(anyLong());
        verify(applicationRepository).save(isA(Application.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findApplicationShouldThrowExceptionDueToNullId(){
        Optional<Application> application = applicationService.findApplication(null);
    }

    @Test
    public void findApplicationShouldReturnApplicationObject(){
        Application application = new Application("test", "testversion","owner");
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        Optional<Application> result = applicationService.findApplication((long) 0);
        assertTrue(result.isPresent());
    }

    @Test
    public void findAllShouldReturnList(){
        List<Application> testList = new ArrayList<>();
        Application test = new Application("test", "testversion","owner");
        testList.add(test);
        when(applicationRepository.findAll()).thenReturn(testList);
        List<Application> result = applicationService.findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

}
