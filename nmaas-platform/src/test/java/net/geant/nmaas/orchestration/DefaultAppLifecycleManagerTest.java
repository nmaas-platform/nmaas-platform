package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.NmServiceRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesRepositoryManager;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRestartActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpdateConfigurationEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DefaultAppLifecycleManagerTest {

    private AppDeploymentRepositoryManager repositoryManager = mock(AppDeploymentRepositoryManager.class);
    private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private NmServiceRepositoryManager serviceRepositoryManager = mock(KubernetesRepositoryManager.class);
    private JanitorService janitorService = mock(JanitorService.class);

    private DefaultAppLifecycleManager appLifecycleManager;

    @BeforeEach
    public void setup() {
        appLifecycleManager = new DefaultAppLifecycleManager(repositoryManager, eventPublisher, serviceRepositoryManager, janitorService);
    }

    @Test
    public void shouldGenerateProperIdentifier() {
        when(repositoryManager.load(any())).thenThrow(new InvalidDeploymentIdException());
        Identifier id = appLifecycleManager.generateDeploymentId();
        assertThat(id.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?"), is(true));
    }

    @Test
    public void shouldTriggerAppInstanceDeployment() {
        when(repositoryManager.load(any())).thenThrow(new InvalidDeploymentIdException());
        AppDeployment appDeployment = new AppDeployment();
        Identifier assignedID = appLifecycleManager.deployApplication(appDeployment);
        assertThat(assignedID, is(notNullValue()));
        verify(repositoryManager, times(1))
                .store(argThat((AppDeployment arg) -> arg.getDeploymentId().equals(assignedID)));
        verify(eventPublisher, times(1))
                .publishEvent(argThat((AppVerifyRequestActionEvent arg) -> arg.getRelatedTo().equals(assignedID)));
    }

    @Test
    public void shouldNotTriggerAppInstanceConfiguration() throws Throwable {
        when(repositoryManager.load(any())).thenReturn(new AppDeployment());
        when(serviceRepositoryManager.loadService(any())).thenReturn(new KubernetesNmServiceInfo());
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getStorageSpace()).thenReturn(null);
        when(configurationView.getJsonInput()).thenReturn("");
        appLifecycleManager.applyConfiguration(new Identifier(), configurationView);
        verify(repositoryManager, times(1)).update(any());
        verify(serviceRepositoryManager, times(0)).updateStorageSpace(any(), anyInt());
        verify(serviceRepositoryManager, times(0)).addAdditionalParameters(any(), anyMap());
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    public void shouldNotTriggerAppInstanceConfigurationButPopulateAdditionalParameters() {
        when(repositoryManager.load(any())).thenReturn(new AppDeployment());
        when(serviceRepositoryManager.loadService(any())).thenReturn(new KubernetesNmServiceInfo());
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getStorageSpace()).thenReturn(10);
        when(configurationView.getAdditionalParameters()).thenReturn("{\"keyadd1\": \"valadd1\"}");
        when(configurationView.getMandatoryParameters()).thenReturn("{\"keyman1\": \"valman1\", \"keyman2\": \"valman2\"}");
        when(configurationView.getJsonInput()).thenReturn("");
        appLifecycleManager.applyConfiguration(Identifier.newInstance(1L), configurationView);
        ArgumentCaptor<Identifier> idArg = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<Map<String, String>> mapArg = ArgumentCaptor.forClass(Map.class);
        verify(serviceRepositoryManager, times(1)).updateStorageSpace(Identifier.newInstance(1L), 10);
        verify(serviceRepositoryManager, times(2)).addAdditionalParameters(idArg.capture(), mapArg.capture());
        assertThat(mapArg.getAllValues().get(0).size(), is(1));
        assertThat(mapArg.getAllValues().get(1).size(), is(2));
    }

    @Test
    public void shouldTriggerAppInstanceConfigurationInCorrectState() throws Throwable {
        when(repositoryManager.load(any())).thenReturn(AppDeployment.builder().state(AppDeploymentState.MANAGEMENT_VPN_CONFIGURED).build());
        when(serviceRepositoryManager.loadService(any())).thenReturn(new KubernetesNmServiceInfo());
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getJsonInput()).thenReturn("");
        appLifecycleManager.applyConfiguration(new Identifier(), configurationView);
        verify(eventPublisher, times(1)).publishEvent(any(AppApplyConfigurationActionEvent.class));
    }

    @Test
    public void shouldTriggerAppInstanceConfigurationUpdate() {
        when(repositoryManager.load(any())).thenReturn(AppDeployment.builder().configuration(new AppConfiguration()).build());
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getJsonInput()).thenReturn("{config}");
        appLifecycleManager.updateConfiguration(new Identifier(), configurationView);
        verify(repositoryManager, times(1)).update(any());
        verify(eventPublisher, times(1)).publishEvent(any(AppUpdateConfigurationEvent.class));
    }

    @Test
    public void shouldNotTriggerAppInstanceConfigurationUpdateIfConfigIsNull() {
        when(repositoryManager.load(any())).thenReturn(new AppDeployment());
        AppConfigurationView configurationView = new AppConfigurationView();
        appLifecycleManager.updateConfiguration(new Identifier(), configurationView);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    public void shouldNotTriggerAppInstanceConfigurationUpdateIfConfigIsEmpty() {
        when(repositoryManager.load(any())).thenReturn(new AppDeployment());
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getJsonInput()).thenReturn("");
        appLifecycleManager.updateConfiguration(new Identifier(), configurationView);
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    public void shouldTriggerAppInstanceReDeployment() {
        appLifecycleManager.redeployApplication(new Identifier());
        verify(eventPublisher, times(1)).publishEvent(any(NmServiceDeploymentStateChangeEvent.class));
        verify(eventPublisher, times(1)).publishEvent(any(AppVerifyRequestActionEvent.class));
    }

    @Test
    public void shouldTriggerAppInstanceRemoval() {
        when(repositoryManager.loadState(any())).thenReturn(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        appLifecycleManager.removeApplication(new Identifier());
        verify(eventPublisher, times(1)).publishEvent(any(AppRemoveActionEvent.class));
    }

    @Test
    public void shouldNotTriggerAppInstanceRemovalIfAlreadyRemoved() {
        when(repositoryManager.loadState(any())).thenReturn(AppDeploymentState.APPLICATION_REMOVED);
        appLifecycleManager.removeApplication(new Identifier());
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    public void shouldTriggerAppInstanceUpdate() {
        assertThrows(NotImplementedException.class, () -> {
            appLifecycleManager.updateApplication(new Identifier(), new Identifier());
        });
    }

    @Test
    public void shouldTriggerAppInstanceRestart() {
        appLifecycleManager.restartApplication(new Identifier());
        verify(eventPublisher, times(1)).publishEvent(any(AppRestartActionEvent.class));
    }

    @Test
    public void shouldReplaceHashToDotsInMapKeys() {
        Map<String, String> input = new HashMap<>();
        input.put("keywith#", "value");
        input.put("keywith#inthemiddle", "value");
        input.put("keywith#andnullvalue", null);
        input.put("keywith#andemptyvalue", "");
        Map<String, String> output = DefaultAppLifecycleManager.replaceHashToDotsInMapKeys(input);
        assertThat(output.keySet().size(), is(2));
        assertThat(output.keySet().containsAll(Arrays.asList("keywith.", "keywith.inthemiddle")), is(true));
    }

    @Test
    public void shouldAddQuotesInMapValuesWhereRequired() {
        Map<String, String> input = new HashMap<>();
        input.put("keywith#", "value");
        input.put("keywith#inthemiddle", "value, this value and another value");
        Map<String, String> output = DefaultAppLifecycleManager.replaceHashToDotsInMapKeys(input);
        assertThat(output.values().containsAll(Arrays.asList("value", "\"value\\, this value and another value\"")), is(true));
    }

    @Test
    public void shouldGetMapFromJson() {
        Map<String, String> map = appLifecycleManager.getMapFromJson("{\"key1\": \"val1\", \"key2\": \"val2\"}");
        assertThat(map.keySet().size(), is(2));
        assertThat(map.get("key1"), allOf(is(notNullValue()), is(equalTo("val1"))));
        assertThat(map.get("key2"), allOf(is(notNullValue()), is(equalTo("val2"))));
    }

}
