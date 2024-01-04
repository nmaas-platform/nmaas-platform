package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.orchestration.AppComponentDetails;
import net.geant.nmaas.orchestration.AppComponentLogs;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.api.logs.PodInfo;
import net.geant.nmaas.portal.api.logs.PodLogs;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.ApplicationLogsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationLogsServiceImplTest {

    private static final long APP_INSTANCE_ID = 1L;
    private static final String POD_NAME = "pod1";
    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");

    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final AppDeploymentMonitor appDeploymentMonitor = mock(AppDeploymentMonitor.class);

    private AppInstance appInstance;

    ApplicationLogsService applicationLogsService;

    @BeforeEach
    void setup() {
        Application application = new Application(100L, "app", "version");
        application.setAppDeploymentSpec(AppDeploymentSpec.builder().allowLogAccess(true).build());
        appInstance = new AppInstance(APP_INSTANCE_ID, application, null, "appInstance1", false);
        appInstance.setInternalId(DEPLOYMENT_ID);

        applicationLogsService = new ApplicationLogsServiceImpl(applicationInstanceService, appDeploymentMonitor);
    }

    @Test
    void shouldCheckIfLogAccessIsAllowed() {
        when(applicationInstanceService.find(APP_INSTANCE_ID)).thenReturn(Optional.of(appInstance));
        assertThat(applicationLogsService.isLogAccessEnabled(APP_INSTANCE_ID)).isTrue();
    }

    @Test
    void shouldGetNamesOfPods() {
        when(applicationInstanceService.find(APP_INSTANCE_ID)).thenReturn(Optional.of(appInstance));
        when(appDeploymentMonitor.appComponents(DEPLOYMENT_ID)).thenReturn(
                List.of(new AppComponentDetails("p1", "pd1"), new AppComponentDetails("p2", "pd2")));
        List<PodInfo> podNames = applicationLogsService.getPodNames(APP_INSTANCE_ID);
        assertThat(podNames.size()).isEqualTo(2);
        assertThat(podNames).extracting(PodInfo::getName).containsAll(List.of("p1", "p2"));
    }

    @Test
    void shouldGetPodLogs() {
        when(applicationInstanceService.find(APP_INSTANCE_ID)).thenReturn(Optional.of(appInstance));
        when(appDeploymentMonitor.appComponentLogs(DEPLOYMENT_ID, POD_NAME)).thenReturn(
                new AppComponentLogs(POD_NAME, List.of("l1", "l2", "l3")));
        PodLogs podLogs = applicationLogsService.getPodLogs(APP_INSTANCE_ID, POD_NAME);
        assertThat(podLogs.getName()).isEqualTo(POD_NAME);
        assertThat(podLogs.getLines()).containsAll(List.of("l1", "l2", "l3"));
    }

}
