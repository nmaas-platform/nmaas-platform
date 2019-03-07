package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceImplTest {

    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    ApplicationServiceImpl applicationService;

    @BeforeEach
    public void setup(){
        applicationService = new ApplicationServiceImpl(applicationRepository);
    }

    @Test
    public void createMethodShouldThrowExceptionDueToIncorrectName(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.create(null);
        });
    }

    @Test
    public void createMethodShouldReturnApplicationObject(){
        Application application = new Application("test");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        Application result = applicationService.create("test");
        assertNotNull(result);
        assertEquals("test", result.getName());
    }

    @Test
    public void updateMethodShouldThrowExceptionDueToNullPassedAsParameter(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.update(null);
        });
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

    @Test
    public void deleteMethodShouldTrowExceptionDueToNullPassedAsId(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.delete(null);
        });
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

    @Test
    public void findApplicationShouldThrowExceptionDueToNullId(){
        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.findApplication(null);
        });
    }

    @Test
    public void findApplicationShouldReturnApplicationObject(){
        Application application = new Application("test");
        when(applicationRepository.findById(anyLong())).thenReturn(Optional.of(application));
        Optional<Application> result = applicationService.findApplication((long) 0);
        assertTrue(result.isPresent());
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
