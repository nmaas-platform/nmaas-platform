package net.geant.nmaas.orchestration.api;

import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.AppUiAccessDetails;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.api.model.AppDeploymentView;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidAppStateException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppDeploymentMonitorRestControllerTest {

    @Mock
    private AppDeploymentMonitor deploymentMonitor;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AppDeploymentMonitorRestController controller;

    @Test
    void shouldListAllDeploymentsMappedToView() {
        AppDeployment deployment1 = AppDeployment.builder().deploymentId(Identifier.newInstance("dep-1")).build();
        AppDeployment deployment2 = AppDeployment.builder().deploymentId(Identifier.newInstance("dep-2")).build();
        AppDeploymentView view1 = new AppDeploymentView("dep-1",
                "name-1",
                "domain-1",
                "REQUESTED",
                "owner-1",
                "app-1");
        AppDeploymentView view2 = new AppDeploymentView("dep-2",
                "name-2",
                "domain-2",
                "REQUESTED",
                "owner-2",
                "app-2");

        when(deploymentMonitor.allDeployments()).thenReturn(List.of(deployment1, deployment2));
        when(modelMapper.map(deployment1, AppDeploymentView.class)).thenReturn(view1);
        when(modelMapper.map(deployment2, AppDeploymentView.class)).thenReturn(view2);

        List<AppDeploymentView> result = controller.listAllDeployments();

        assertEquals(2, result.size());
        assertSame(view1, result.get(0));
        assertSame(view2, result.get(1));
    }

    @Test
    void shouldLoadDeploymentStateUsingIdentifierFromPathVariable() {
        when(deploymentMonitor.state(Identifier.newInstance("dep-1")))
                .thenReturn(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED);

        AppLifecycleState result = controller.loadDeploymentState("dep-1");

        ArgumentCaptor<Identifier> identifierCaptor = ArgumentCaptor.forClass(Identifier.class);
        verify(deploymentMonitor).state(identifierCaptor.capture());
        assertEquals("dep-1", identifierCaptor.getValue().value());
        assertEquals(AppLifecycleState.APPLICATION_DEPLOYMENT_VERIFIED, result);
    }

    @Test
    void shouldLoadDeploymentAccessDetailsUsingIdentifierFromPathVariable() {
        AppUiAccessDetails accessDetails = new AppUiAccessDetails(Set.of());
        when(deploymentMonitor.userAccessDetails(Identifier.newInstance("dep-1"))).thenReturn(accessDetails);

        AppUiAccessDetails result = controller.loadDeploymentUserAccessInfo("dep-1");

        ArgumentCaptor<Identifier> identifierCaptor = ArgumentCaptor.forClass(Identifier.class);
        verify(deploymentMonitor).userAccessDetails(identifierCaptor.capture());
        assertEquals("dep-1", identifierCaptor.getValue().value());
        assertSame(accessDetails, result);
    }

    @Test
    void shouldHandleInvalidDeploymentIdException() {
        String result = controller.handleInvalidDeploymentIdException(
                new InvalidDeploymentIdException("missing deployment"));
        assertEquals("missing deployment", result);
    }

    @Test
    void shouldHandleInvalidAppStateException() {
        String result = controller.handleInvalidAppStateException(new InvalidAppStateException("invalid state"));
        assertEquals("invalid state", result);
    }
}

