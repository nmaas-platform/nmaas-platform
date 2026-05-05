package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.api.dto.applications.AppConfigurationDto;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.repositories.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppLifecycleManagerRestControllerTest {

    @Mock
    private AppLifecycleManager lifecycleManager;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private AppLifecycleManagerRestController controller;

    @Test
    void shouldDeployApplicationForRequestedDomainApplicationAndName() {
        Application application = new Application(15L, "app-name", "1.0.0");
        Identifier deploymentId = Identifier.newInstance("deployment-1");

        when(applicationRepository.findById(15L)).thenReturn(Optional.of(application));
        when(lifecycleManager.deployApplication(org.mockito.ArgumentMatchers.any(AppDeployment.class)))
                .thenReturn(deploymentId);

        Identifier result = controller.deployApplication("domain-1",
                "15",
                "my-deployment");

        ArgumentCaptor<AppDeployment> deploymentCaptor = ArgumentCaptor.forClass(AppDeployment.class);
        verify(lifecycleManager).deployApplication(deploymentCaptor.capture());
        AppDeployment capturedDeployment = deploymentCaptor.getValue();
        assertEquals("domain-1", capturedDeployment.getDomain());
        assertEquals("15", capturedDeployment.getApplicationId().value());
        assertEquals("my-deployment", capturedDeployment.getDeploymentName());
        assertEquals("app-name", capturedDeployment.getAppName());
        assertSame(deploymentId, result);
    }

    @Test
    void shouldThrowWhenApplicationNotFoundDuringDeploymentRequest() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.deployApplication("domain-1", "99", "my-deployment")
        );
        assertEquals("Application not found", ex.getMessage());
    }

    @Test
    void shouldApplyConfigurationForDeploymentAndPrincipal() {
        AppConfigurationDto configuration = AppConfigurationDto.builder().build();
        Principal principal = () -> "test-user";

        controller.applyConfiguration("dep-1", configuration, principal);

        verify(lifecycleManager).applyConfiguration(
                Identifier.newInstance("dep-1"), configuration, "test-user");
    }

    @Test
    void shouldUpdateConfigurationForDeployment() {
        AppConfigurationDto configuration = AppConfigurationDto.builder().build();

        controller.updateConfiguration("dep-1", configuration);

        verify(lifecycleManager).updateConfiguration(Identifier.newInstance("dep-1"), configuration);
    }

    @Test
    void shouldRemoveApplicationForDeployment() {
        controller.removeApplication("dep-1");
        verify(lifecycleManager).removeApplication(Identifier.newInstance("dep-1"));
    }

    @Test
    void shouldHandleInvalidDeploymentIdException() {
        String result = controller.handleInvalidDeploymentIdException(
                new InvalidDeploymentIdException("missing deployment"));
        assertEquals("missing deployment", result);
    }
}
