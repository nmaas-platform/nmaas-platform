package net.geant.nmaas.portal.api.market;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;


public class AppInstanceControllerTest {

    private AppLifecycleManager appLifecycleManager = mock(AppLifecycleManager.class);
    private AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);
    private DomainService domainService = mock(DomainService.class);
    private ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);

    private AppInstanceController appInstanceController;

    private Application application;
    private Domain domain;
    private String name = "app1";
    private String templateString = "{\"template\":\"xD\"}";
    private String identifierValue = "id12";
    private User owner;

    private Pageable pageable = mock(Pageable.class);

    @BeforeEach
    public void setup() {
        owner = new User("admin");
        domain = new Domain("domain one", "dom-1");
        application = new Application(name,"1.0","admin");

        ConfigWizardTemplate configWizardTemplate = mock(ConfigWizardTemplate.class);
        when(configWizardTemplate.getTemplate()).thenReturn(templateString);
        application.setConfigWizardTemplate(configWizardTemplate);

        appInstanceController = new AppInstanceController(
                appLifecycleManager,
                appDeploymentMonitor,
                applicationInstanceService,
                domainService,
                appDeploymentRepositoryManager);
        appInstanceController.modelMapper = new ModelMapper();

        when(pageable.getOffset()).thenReturn(0L);
        when(pageable.getPageNumber()).thenReturn(0);
        when(pageable.getPageSize()).thenReturn(20);
        when(pageable.getSort()).thenReturn(null);
    }

    @Test
    public void shouldReturnAllInstances() {
        AppInstance appInstance = new AppInstance(application, name, domain, owner);
        List<AppInstance> appInstanceList = new ArrayList<>();
        appInstanceList.add(appInstance);
        Page<AppInstance> appInstancePage = new PageImpl<>(appInstanceList);

        when(applicationInstanceService.findAll(pageable)).thenReturn(appInstancePage);
        when(appDeploymentMonitor.userAccessDetails(any())).thenThrow(new InvalidDeploymentIdException());
        AppDeployment appDeployment = mock(AppDeployment.class);
        when(appDeployment.getDescriptiveDeploymentId()).thenReturn(new Identifier(identifierValue));
        when(appDeploymentRepositoryManager.load(any())).thenReturn(appDeployment);


        List<AppInstanceView> result = appInstanceController.getAllInstances(pageable);

        assertEquals(1, result.size());
        AppInstanceView appInstanceView = result.get(0);
        assertEquals(name, appInstanceView.getApplicationName());
        assertEquals(owner.getUsername(), appInstanceView.getOwner().getUsername());
        assertEquals(identifierValue, appInstanceView.getDescriptiveDeploymentId());
    }

    @Test
    public void shouldGetAllMyInstances() {

    }

    @Test
    public void shouldGetUserDomainAppInstances() {

    }
}
