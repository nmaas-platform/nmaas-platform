package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.kubernetes.remote.RemoteClusterManager;
import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AppRequestVerificationTaskTest {

    private final NmServiceDeploymentProvider deploy = mock(NmServiceDeploymentProvider.class);
    private final AppDeploymentRepository deployments = mock(AppDeploymentRepository.class);
    private final ApplicationRepository applications = mock(ApplicationRepository.class);
    private final RemoteClusterManager remoteClusterManager = mock(RemoteClusterManager.class);

    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("deploymentId");
    private static final Identifier DEPLOYMENT_ID_2 = Identifier.newInstance("deploymentId2");

    private final AppRequestVerificationTask task = new AppRequestVerificationTask(deployments, applications, remoteClusterManager, deploy);

    @Test
    void shouldTriggerRequestVerify() throws InterruptedException {
        when(deployments.findByDeploymentId(DEPLOYMENT_ID)).thenReturn(Optional.of(AppDeployment.builder().applicationId(Identifier.newInstance(10L)).build()));
        when(applications.findById(any(Long.class))).thenReturn(Optional.of(new Application()));
        when(remoteClusterManager.clusterExists(any())).thenReturn(true);

        task.trigger(new AppVerifyRequestActionEvent(this, DEPLOYMENT_ID));

        verify(deploy, times(1)).verifyRequest(any(Identifier.class), any(AppDeployment.class), isNull());
    }

    @Test
    void shouldNotTriggerRequestVerifyIfExceptionRaised() throws InterruptedException {
        when(deployments.findByDeploymentId(DEPLOYMENT_ID_2)).thenReturn(Optional.empty());

        task.trigger(new AppVerifyRequestActionEvent(this, DEPLOYMENT_ID_2));

        verifyNoMoreInteractions(deploy);
    }

}
