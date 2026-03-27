package net.geant.nmaas.orchestration.tasks.dcn;

import net.geant.nmaas.dcn.deployment.DcnDeploymentProvider;
import net.geant.nmaas.dcn.deployment.DcnDeploymentProvidersManager;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.entities.DomainDcnDetails;
import net.geant.nmaas.dcn.deployment.repositories.DomainDcnDetailsRepository;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DcnRequestVerificationTaskTest {

    private final DcnDeploymentProvidersManager providersManager = mock(DcnDeploymentProvidersManager.class);
    private final DomainDcnDetailsRepository repository = mock(DomainDcnDetailsRepository.class);
    private final DcnDeploymentProvider deploymentProvider = mock(DcnDeploymentProvider.class);

    private final DcnRequestVerificationTask task = new DcnRequestVerificationTask(providersManager, repository);

    @Test
    void shouldVerifyDcnRequestUsingConstructedSpec() {
        String domain = "test-domain";
        DomainDcnDetails details = new DomainDcnDetails();
        details.setDomainCodename(domain);
        details.setDcnDeploymentType(DcnDeploymentType.MANUAL);

        when(repository.findByDomainCodename(domain)).thenReturn(Optional.of(details));
        when(providersManager.getDcnDeploymentProvider(domain)).thenReturn(deploymentProvider);

        task.trigger(new DcnVerifyRequestActionEvent(this, domain));

        verify(deploymentProvider).verifyRequest(
                org.mockito.ArgumentMatchers.eq(domain),
                argThat(spec -> spec.getDomain().equals(domain)
                        && spec.getDcnDeploymentType() == DcnDeploymentType.MANUAL
                        && spec.getName().startsWith(domain + "-"))
        );
    }

    @Test
    void shouldNotThrowWhenDomainDetailsAreMissing() {
        String domain = "test-domain";
        when(repository.findByDomainCodename(domain)).thenReturn(Optional.empty());
        when(providersManager.getDcnDeploymentProvider(domain)).thenReturn(deploymentProvider);

        assertDoesNotThrow(() -> task.trigger(new DcnVerifyRequestActionEvent(this, domain)));
        verify(providersManager).getDcnDeploymentProvider(domain);
        verifyNoInteractions(deploymentProvider);
    }

    @Test
    void shouldNotThrowWhenVerificationProviderFails() {
        String domain = "test-domain";
        DomainDcnDetails details = new DomainDcnDetails();
        details.setDomainCodename(domain);
        details.setDcnDeploymentType(DcnDeploymentType.MANUAL);

        when(repository.findByDomainCodename(domain)).thenReturn(Optional.of(details));
        when(providersManager.getDcnDeploymentProvider(domain)).thenReturn(deploymentProvider);
        doThrow(new RuntimeException("failure")).when(deploymentProvider)
                .verifyRequest(org.mockito.ArgumentMatchers.eq(domain), org.mockito.ArgumentMatchers.any());

        assertDoesNotThrow(() -> task.trigger(new DcnVerifyRequestActionEvent(this, domain)));
        verify(providersManager).getDcnDeploymentProvider(domain);
    }
}
