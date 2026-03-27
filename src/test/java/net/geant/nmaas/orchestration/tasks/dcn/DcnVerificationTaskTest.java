package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvidersManager;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyActionEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DcnVerificationTaskTest {

    private final DcnDeploymentProvidersManager providersManager = mock(DcnDeploymentProvidersManager.class);
    private final DcnDeploymentProvider deploymentProvider = mock(DcnDeploymentProvider.class);

    private final DcnVerificationTask task = new DcnVerificationTask(providersManager);

    @Test
    void shouldVerifyDcnForDomainFromEvent() {
        String domain = "test-domain";
        when(providersManager.getDcnDeploymentProvider(domain)).thenReturn(deploymentProvider);

        task.trigger(new DcnVerifyActionEvent(this, domain));

        verify(deploymentProvider).verifyDcn(domain);
    }

    @Test
    void shouldNotThrowWhenVerificationProviderFails() {
        String domain = "test-domain";
        when(providersManager.getDcnDeploymentProvider(domain)).thenReturn(deploymentProvider);
        doThrow(new RuntimeException("failure")).when(deploymentProvider).verifyDcn(domain);

        assertDoesNotThrow(() -> task.trigger(new DcnVerifyActionEvent(this, domain)));
        verify(providersManager).getDcnDeploymentProvider(domain);
    }

    @Test
    void shouldNotThrowWhenProviderLookupFails() {
        String domain = "test-domain";
        doThrow(new RuntimeException("failure")).when(providersManager).getDcnDeploymentProvider(domain);

        assertDoesNotThrow(() -> task.trigger(new DcnVerifyActionEvent(this, domain)));
        verifyNoInteractions(deploymentProvider);
    }
}
