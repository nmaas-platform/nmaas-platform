package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationLogsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class ApplicationLogsServiceImplTest {

    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);

    ApplicationLogsService applicationLogsService;

    @BeforeEach
    void setup() {
        applicationLogsService = new ApplicationLogsServiceImpl(applicationInstanceService, appDeploymentMonitor);
    }

    @Test
    void updateMethodShouldThrowExceptionDueToNullPassedAsParameter(){
        applicationLogsService.getPodNames(1L);
    }

}
