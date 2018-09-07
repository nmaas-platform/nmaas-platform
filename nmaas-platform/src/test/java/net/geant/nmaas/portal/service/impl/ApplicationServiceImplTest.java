package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.persistent.entity.projections.ApplicationBriefProjection;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.Before;
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
        applicationService.create(null);
    }

    @Test
    public void createMethodShouldReturnApplicationObject(){
        Application application = new Application("test");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        Application result = applicationService.create("test");
        assertNotNull(result);
        assertEquals("test", result.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateMethodShouldThrowExceptionDueToNullPassedAsParameter(){
        applicationService.update(null);
    }

    @Test
    public void updateMethodShouldReturnApplicationObject(){
        Application application = new Application("test");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        application.setName("test2");
        Application result = applicationService.update(application);
        assertNotNull(result);
        assertNotEquals("test", result.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteMethodShouldTrowExceptionDueToNullPassedAsId(){
        applicationService.delete(null);
    }

    @Test
    public void deleteMethodShouldSetApplicationAsDeleted(){
        Application application = new Application("test");
        application.setId((long) 0);
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
        Application application = new Application("test");
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        Optional<Application> result = applicationService.findApplication((long) 0);
        assertTrue(result.isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findApplicationBriefShouldThrowExceptionDueToNullId(){
        Optional<ApplicationBriefProjection> applicationBriefProjection = applicationService.findApplicationBrief(null);
    }

    @Test
    public void findApplicationBriefShouldReturnApplicationBriefProjectionObject(){
        ApplicationBriefProjection applicationBriefProjection = mock(ApplicationBriefProjection.class);
        when(applicationRepository.findApplicationBriefById(anyLong())).thenReturn(Optional.of(applicationBriefProjection));
        Optional<ApplicationBriefProjection> result = applicationService.findApplicationBrief((long) 0);
        assertTrue(result.isPresent());
    }

    @Test
    public void findAllBriefShouldReturnListOfApplicationBriefProjections(){
        ApplicationBriefProjection applicationBriefProjection1 = mock(ApplicationBriefProjection.class);
        ApplicationBriefProjection applicationBriefProjection2 = mock(ApplicationBriefProjection.class);
        ApplicationBriefProjection applicationBriefProjection3 = mock(ApplicationBriefProjection.class);
        List<ApplicationBriefProjection> testList = new ArrayList<>();
        testList.add(applicationBriefProjection1);
        testList.add(applicationBriefProjection2);
        testList.add(applicationBriefProjection3);
        when(applicationRepository.findApplicationBriefAll()).thenReturn(testList);
        List<ApplicationBriefProjection> resultList = applicationService.findAllBrief();
        assertEquals(3, resultList.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAllBriefWithParameterThrowExceptionDueToInvalidIds(){
        applicationService.findAllBrief((List<Long>) null);
    }

    @Test
    public void findAllBriefWithParameterShouldReturnListOfApplicationBriefProjections(){
        List<Long> testIdsList = new ArrayList<>();
        testIdsList.add((long) 0);
        testIdsList.add((long) 1);
        ApplicationBriefProjection applicationBriefProjection1 = mock(ApplicationBriefProjection.class);
        ApplicationBriefProjection applicationBriefProjection2 = mock(ApplicationBriefProjection.class);
        List<ApplicationBriefProjection> testList = new ArrayList<>();
        testList.add(applicationBriefProjection1);
        testList.add(applicationBriefProjection2);
        when(applicationRepository.findApplicationBriefAllByIdIn(anyList())).thenReturn(testList);
        List<ApplicationBriefProjection> result = applicationService.findAllBrief(testIdsList);
        assertEquals(testIdsList.size(), result.size());
    }

    @Test
    public void findAllShouldReturnList(){
        List<Application> testList = new ArrayList<>();
        Application test = new Application("test");
        testList.add(test);
        when(applicationRepository.findAll()).thenReturn(testList);
        List<Application> result = applicationService.findAll();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

}
