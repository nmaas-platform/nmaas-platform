package net.geant.nmaas.portal.service.impl;

import java.util.Collections;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesChart;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.domain.AppDescriptionView;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import net.geant.nmaas.portal.persistent.entity.ConfigTemplate;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isA;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceImplTest {

    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    ApplicationServiceImpl applicationService;

    @Before
    public void setup(){
        applicationService = new ApplicationServiceImpl(applicationRepository, new ModelMapper());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createMethodShouldThrowExceptionDueToIncorrectName(){
        applicationService.create(null, null);
    }

    @Test
    public void createMethodShouldReturnApplicationObject(){
        Application application = new Application("test","testversion","owner");
        when(applicationRepository.save(isA(Application.class))).thenReturn(application);
        ApplicationView applicationView = new ApplicationView();
        applicationView.setName("test");
        applicationView.setVersion("testversion");
        applicationView.setOwner("owner");
        applicationView.setDescriptions(Collections.singletonList(new AppDescriptionView("en", "test", "testfull")));
        applicationView.setAppDeploymentSpec(new net.geant.nmaas.portal.api.domain.AppDeploymentSpec());
        applicationView.setConfigTemplate(new net.geant.nmaas.portal.api.domain.ConfigTemplate("template"));
        Application result = applicationService.create(applicationView,"owner");
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
        application.setAppDeploymentSpec(new AppDeploymentSpec());
        application.getAppDeploymentSpec().setDefaultStorageSpace(1);
        application.getAppDeploymentSpec().setKubernetesTemplate(new KubernetesTemplate());
        application.getAppDeploymentSpec().getKubernetesTemplate().setChart(new KubernetesChart("chart", "version"));
        application.setConfigTemplate(new ConfigTemplate("test-template"));
        application.setDescriptions(Collections.singletonList(new AppDescription()));
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
