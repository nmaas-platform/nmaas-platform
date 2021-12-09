package net.geant.nmaas.orchestration.tasks.app;

import net.geant.nmaas.nmservice.deployment.NmServiceDeploymentProvider;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AppRequestVerificationTaskTest {

    private NmServiceDeploymentProvider deploy = mock(NmServiceDeploymentProvider.class);
    private AppDeploymentRepository deployments = mock(AppDeploymentRepository.class);
    private ApplicationRepository applications = mock(ApplicationRepository.class);

    private AppRequestVerificationTask task;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private Identifier deploymentId2 = Identifier.newInstance("deploymentId2");

    @BeforeEach
    public void setup() {
        task = new AppRequestVerificationTask(deploy, deployments, applications);
    }

    @Test
    public void shouldTriggerRequestVerify() {
        when(deployments.findByDeploymentId(deploymentId)).thenReturn(Optional.of(AppDeployment.builder().applicationId(Identifier.newInstance(10L)).build()));
        when(applications.findById(any(Long.class))).thenReturn(Optional.of(new Application()));
        task.trigger(new AppVerifyRequestActionEvent(this, deploymentId));
        verify(deploy, times(1)).verifyRequest(any(Identifier.class), any(AppDeployment.class), isNull());
    }

    @Test
    public void shouldNotTriggerRequestVerifyIfExceptionRaised() {
        when(deployments.findByDeploymentId(deploymentId2)).thenReturn(Optional.empty());
        task.trigger(new AppVerifyRequestActionEvent(this, deploymentId2));
        verifyNoMoreInteractions(deploy);
    }

}
