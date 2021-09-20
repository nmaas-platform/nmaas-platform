package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.helm;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.HelmChartRepositoryEmbeddable;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.events.ApplicationListUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static net.geant.nmaas.portal.events.ApplicationListUpdatedEvent.ApplicationAction.ADDED;
import static net.geant.nmaas.portal.events.ApplicationListUpdatedEvent.ApplicationAction.DELETED;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class HelmChartUpdateListenerTest {

    @Mock
    private HelmCommandExecutor executor;

    private HelmChartUpdateListener listener;

    @BeforeEach
    public void setup() {
        listener = new HelmChartUpdateListener(executor);
    }

    @Test
    public void shouldSkipExecutionDueToIncorrectAction() {
        ApplicationListUpdatedEvent event = new ApplicationListUpdatedEvent(this, "app", "1.0", DELETED, null);
        listener.trigger(event);
        verifyNoInteractions(executor);
    }

    @Test
    public void shouldSkipExecutionDueToIncorrectHelmChart() {
        AppDeploymentSpec spec = new AppDeploymentSpec();
        KubernetesTemplate template = new KubernetesTemplate();
        spec.setKubernetesTemplate(template);
        ApplicationListUpdatedEvent event = new ApplicationListUpdatedEvent(this, "app", "1.0", DELETED, spec);
        listener.trigger(event);
        verifyNoInteractions(executor);
    }

    @Test
    public void shouldExecuteHelmRepoAddCommand() {
        AppDeploymentSpec spec = new AppDeploymentSpec();
        KubernetesTemplate template = new KubernetesTemplate();
        HelmChartRepositoryEmbeddable repository = new HelmChartRepositoryEmbeddable();
        repository.setName("name");
        repository.setUrl("url");
        template.setHelmChartRepository(repository);
        spec.setKubernetesTemplate(template);
        ApplicationListUpdatedEvent event = new ApplicationListUpdatedEvent(this, "app", "1.0", ADDED, spec);
        listener.trigger(event);
        verify(executor, times(1)).executeHelmRepoAddCommand("name", "url");
    }

}
