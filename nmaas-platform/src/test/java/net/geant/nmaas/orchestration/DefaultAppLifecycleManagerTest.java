package net.geant.nmaas.orchestration;

import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.components.janitor.JanitorService;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesNmServiceInfo;
import net.geant.nmaas.nmservice.deployment.entities.NmServiceInfo;
import net.geant.nmaas.nmservice.deployment.repository.NmServiceInfoRepository;
import net.geant.nmaas.orchestration.api.model.AppConfigurationView;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppApplyConfigurationActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRemoveActionEvent;
import net.geant.nmaas.orchestration.events.app.AppRestartActionEvent;
import net.geant.nmaas.orchestration.events.app.AppUpdateConfigurationEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DefaultAppLifecycleManagerTest {

    private AppDeploymentRepositoryManager repositoryManager = mock(AppDeploymentRepositoryManager.class);
    private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private NmServiceInfoRepository infoRepository = mock(NmServiceInfoRepository.class);
    private JanitorService janitorService = mock(JanitorService.class);

    private DefaultAppLifecycleManager appLifecycleManager;

    @BeforeEach
    public void setup() {
        appLifecycleManager = new DefaultAppLifecycleManager(repositoryManager, eventPublisher, infoRepository, janitorService);
    }

    @Test
    public void shouldGenerateProperIdentifier() {
        when(repositoryManager.load(any())).thenReturn(null);
        Identifier id = appLifecycleManager.generateDeploymentId();
        assertThat(id.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?"), is(true));
    }

    @Test
    public void shouldTriggerAppInstanceDeployment() {
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
        when(infoRepository.findByDeploymentId(any())).thenReturn(Optional.of(new KubernetesNmServiceInfo()));
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getJsonInput()).thenReturn("");
        appLifecycleManager.applyConfiguration(new Identifier(), configurationView);
        verify(repositoryManager, times(1)).update(any());
        ArgumentCaptor<NmServiceInfo> arg = ArgumentCaptor.forClass(NmServiceInfo.class);
        verify(infoRepository, times(1)).save(arg.capture());
        assertThat(arg.getValue().getAdditionalParameters(), is(nullValue()));
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    public void shouldNotTriggerAppInstanceConfigurationButPopulateAdditionalParameters() throws Throwable {
        when(repositoryManager.load(any())).thenReturn(new AppDeployment());
        when(infoRepository.findByDeploymentId(any())).thenReturn(Optional.of(new KubernetesNmServiceInfo()));
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getAdditionalParameters()).thenReturn("{\"keyadd1\": \"valadd1\"}");
        when(configurationView.getMandatoryParameters()).thenReturn("{\"keyman1\": \"valman1\", \"keyman2\": \"valman2\"}");
        when(configurationView.getJsonInput()).thenReturn("");
        appLifecycleManager.applyConfiguration(new Identifier(), configurationView);
        ArgumentCaptor<NmServiceInfo> arg = ArgumentCaptor.forClass(NmServiceInfo.class);
        verify(infoRepository, times(1)).save(arg.capture());
        assertThat(arg.getValue().getAdditionalParameters().size(), is(3));
    }

    @Test
    public void shouldTriggerAppInstanceConfigurationInCorrectState() throws Throwable {
        when(repositoryManager.load(any())).thenReturn(AppDeployment.builder().state(AppDeploymentState.MANAGEMENT_VPN_CONFIGURED).build());
        when(infoRepository.findByDeploymentId(any())).thenReturn(Optional.of(new KubernetesNmServiceInfo()));
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
        appLifecycleManager.removeApplication(new Identifier());
        verify(eventPublisher, times(1)).publishEvent(any(AppRemoveActionEvent.class));
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
        Map<String, String> output = appLifecycleManager.replaceHashToDotsInMapKeys(input);
        assertThat(output.keySet().size(), is(2));
        assertThat(output.keySet().containsAll(Arrays.asList("keywith.", "keywith.inthemiddle")), is(true));
    }

    @Test
    public void shouldGetMapFromJson() {
        Map<String, String> map = appLifecycleManager.getMapFromJson("{\"key1\": \"val1\", \"key2\": \"val2\"}");
        assertThat(map.keySet().size(), is(2));
        assertThat(map.get("key1"), allOf(is(notNullValue()), is(equalTo("val1"))));
        assertThat(map.get("key2"), allOf(is(notNullValue()), is(equalTo("val2"))));
    }

}
