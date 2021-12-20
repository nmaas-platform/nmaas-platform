package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.events.app.AppUpgradeActionEvent;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppUpgradeTaskTest {

    private final NmServiceDeploymentProvider deploymentProvider = mock(NmServiceDeploymentProvider.class);
    private final AppDeploymentRepository deployments = mock(AppDeploymentRepository.class);
    private final ApplicationRepository applications = mock(ApplicationRepository.class);

    private AppUpgradeTask task;

    private final Identifier deploymentId = Identifier.newInstance("deploymentId");
    private final Identifier applicationId = Identifier.newInstance(1L);
    private Application application;

    @BeforeEach
    public void setup() {
        task = new AppUpgradeTask(deploymentProvider, applications);
        application = new Application(1L, "appName", "appVersion");
        application.setAppDeploymentSpec(AppDeploymentSpec.builder().kubernetesTemplate(new KubernetesTemplate()).build());
    }

    @Test
    public void shouldTriggerUpgrade() {
        when(deployments.findByDeploymentId(deploymentId)).thenReturn(Optional.of(AppDeployment.builder().applicationId(applicationId).build()));
        when(applications.findById(any(Long.class))).thenReturn(Optional.of(application));
        task.trigger(new AppUpgradeActionEvent(this, deploymentId, applicationId));
        verify(deploymentProvider, times(1)).upgradeKubernetesService(any(Identifier.class), any(KubernetesTemplate.class));
    }

}
