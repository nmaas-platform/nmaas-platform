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
import net.geant.nmaas.orchestration.events.app.AppUpgradeActionEvent;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class DefaultAppLifecycleManagerTest {

    private final AppDeploymentRepositoryManager repositoryManager = mock(AppDeploymentRepositoryManager.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final NmServiceRepositoryManager<KubernetesNmServiceInfo> serviceRepositoryManager = mock(KubernetesRepositoryManager.class);
    private final JanitorService janitorService = mock(JanitorService.class);
    private final AppTermsAcceptanceService appTermsAcceptanceService = mock(AppTermsAcceptanceService.class);

    private DefaultAppLifecycleManager appLifecycleManager;

    @BeforeEach
    void setup() {
        appLifecycleManager = new DefaultAppLifecycleManager(repositoryManager, eventPublisher, serviceRepositoryManager, janitorService, appTermsAcceptanceService);
    }

    @Test
    void shouldGenerateProperIdentifier() {
        when(repositoryManager.load(any())).thenThrow(new InvalidDeploymentIdException());
        Identifier id = appLifecycleManager.generateDeploymentId();
        assertThat(id.value().matches("[a-z]([-a-z0-9]*[a-z0-9])?"), is(true));
    }

    @Test
    void shouldTriggerAppInstanceDeployment() {
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
    void shouldNotTriggerAppInstanceConfiguration() throws Throwable {
        when(repositoryManager.load(any())).thenReturn(new AppDeployment());
        when(serviceRepositoryManager.loadService(any())).thenReturn(new KubernetesNmServiceInfo());
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getStorageSpace()).thenReturn(null);
        when(configurationView.getJsonInput()).thenReturn("");
        appLifecycleManager.applyConfiguration(new Identifier(), configurationView, "TEST" );
        verify(repositoryManager, times(1)).update(any());
        verify(serviceRepositoryManager, times(0)).updateStorageSpace(any(), anyInt());
        verify(serviceRepositoryManager, times(0)).addAdditionalParameters(any(), anyMap());
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void shouldNotTriggerAppInstanceConfigurationButPopulateAdditionalParameters() {
        when(repositoryManager.load(any())).thenReturn(new AppDeployment());
        when(serviceRepositoryManager.loadService(any())).thenReturn(new KubernetesNmServiceInfo());
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getStorageSpace()).thenReturn(10);
        when(configurationView.getAdditionalParameters()).thenReturn("{\"keyadd1\": \"valadd1\"}");
        when(configurationView.getMandatoryParameters()).thenReturn("{\"keyman1\": \"valman1\", \"keyman2\": \"valman2\"}");
        when(configurationView.getJsonInput()).thenReturn("");
        appLifecycleManager.applyConfiguration(Identifier.newInstance(1L), configurationView, "TEST" );
        ArgumentCaptor<Identifier> idArg = ArgumentCaptor.forClass(Identifier.class);
        ArgumentCaptor<Map<String, String>> mapArg = ArgumentCaptor.forClass(Map.class);
        verify(serviceRepositoryManager, times(1)).updateStorageSpace(Identifier.newInstance(1L), 10);
        verify(serviceRepositoryManager, times(2)).addAdditionalParameters(idArg.capture(), mapArg.capture());
        assertThat(mapArg.getAllValues().get(0).size(), is(1));
        assertThat(mapArg.getAllValues().get(1).size(), is(2));
    }

    @Test
    void shouldTriggerAppInstanceConfigurationInCorrectState() {
        when(repositoryManager.load(any())).thenReturn(AppDeployment.builder().state(AppDeploymentState.MANAGEMENT_VPN_CONFIGURED).build());
        when(serviceRepositoryManager.loadService(any())).thenReturn(new KubernetesNmServiceInfo());
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getJsonInput()).thenReturn("");
        appLifecycleManager.applyConfiguration(new Identifier(), configurationView, "TEST" );
        verify(eventPublisher, times(1)).publishEvent(any(AppApplyConfigurationActionEvent.class));
    }

    @Test
    void shouldNotTriggerAppInstanceConfigurationUpdate() {
        when(repositoryManager.load(any())).thenReturn(AppDeployment.builder().configuration(new AppConfiguration()).build());
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getJsonInput()).thenReturn("{config}");
        appLifecycleManager.updateConfiguration(new Identifier(), configurationView);
        verifyNoMoreInteractions(eventPublisher);
        verifyNoMoreInteractions(janitorService);
    }

    @Test
    void shouldTriggerAppInstanceAuthUpdate() {
        Identifier deploymentId = Identifier.newInstance(1L);
        Identifier descriptiveDeploymentId = Identifier.newInstance("identifier");
        when(repositoryManager.load(deploymentId)).thenReturn(
                AppDeployment.builder()
                        .deploymentId(deploymentId)
                        .descriptiveDeploymentId(descriptiveDeploymentId)
                        .configuration(new AppConfiguration())
                        .build()
        );
        AppConfigurationView configurationView = mock(AppConfigurationView.class);
        when(configurationView.getAccessCredentials()).thenReturn("{\"accessUsername\":\"username\", \"accessPassword\":\"password\"}");
        appLifecycleManager.updateConfiguration(deploymentId, configurationView);
        verify(janitorService, times(1)).createOrReplaceBasicAuth(descriptiveDeploymentId, null, "username", "password");
    }

    @Test
    void shouldTriggerAppInstanceReDeployment() {
        appLifecycleManager.redeployApplication(new Identifier());
        verify(eventPublisher, times(1)).publishEvent(any(NmServiceDeploymentStateChangeEvent.class));
        verify(eventPublisher, times(1)).publishEvent(any(AppVerifyRequestActionEvent.class));
    }

    @Test
    void shouldTriggerAppInstanceRemoval() {
        when(repositoryManager.loadState(any())).thenReturn(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        appLifecycleManager.removeApplication(new Identifier());
        verify(eventPublisher, times(1)).publishEvent(any(AppRemoveActionEvent.class));
    }

    @Test
    void shouldNotTriggerAppInstanceRemovalIfAlreadyRemoved() {
        when(repositoryManager.loadState(any())).thenReturn(AppDeploymentState.APPLICATION_REMOVED);
        appLifecycleManager.removeApplication(new Identifier());
        verifyNoMoreInteractions(eventPublisher);
    }

    @Test
    void shouldTriggerAppInstanceUpgrade() {
        when(repositoryManager.loadState(any())).thenReturn(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        appLifecycleManager.upgradeApplication(new Identifier(), new Identifier());
        verify(eventPublisher, times(1)).publishEvent(any(AppUpgradeActionEvent.class));
    }

    @Test
    void shouldNotTriggerAppInstanceUpgrade() {
        when(repositoryManager.loadState(any())).thenReturn(AppDeploymentState.APPLICATION_DEPLOYMENT_IN_PROGRESS);
        appLifecycleManager.upgradeApplication(new Identifier(), new Identifier());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldTriggerAppInstanceRestart() {
        appLifecycleManager.restartApplication(new Identifier());
        verify(eventPublisher, times(1)).publishEvent(any(AppRestartActionEvent.class));
    }

    @Test
    void shouldReplaceHashToDotsInMapKeys() {
        Map<String, String> input = new HashMap<>();
        input.put("keywith#", "value");
        input.put("keywith#inthemiddle", "value");
        input.put("keywith#andnullvalue", null);
        input.put("keywith#andemptyvalue", "");
        Map<String, String> output = DefaultAppLifecycleManager.replaceHashWithDotInMapKeysAndProcessValues(input);
        assertThat(output.keySet().size(), is(2));
        assertThat(output.keySet().containsAll(Arrays.asList("keywith.", "keywith.inthemiddle")), is(true));
    }

    @Test
    void shouldAddQuotesInMapValuesWhereRequired() {
        Map<String, String> input = new HashMap<>();
        input.put("keywith#", "value");
        input.put("keywith#inthemiddle", "value, this value and another value");
        Map<String, String> output = DefaultAppLifecycleManager.replaceHashWithDotInMapKeysAndProcessValues(input);
        assertThat(output.values().containsAll(Arrays.asList("value", "\"value\\, this value and another value\"")), is(true));
    }

    @Test
    void shouldReplaceHashWithEscapedQuotesInMapValuesWhereRequired() {
        Map<String, String> input = new HashMap<>();
        input.put("key1", "value");
        input.put("key2", "#valuewithhashonbothends#");
        Map<String, String> output = DefaultAppLifecycleManager.replaceHashWithDotInMapKeysAndProcessValues(input);
        assertThat(output.values().containsAll(Arrays.asList("value", "\\\"valuewithhashonbothends\\\"")), is(true));
    }

    @Test
    void shouldGetMapFromJson() {
        Map<String, String> map = appLifecycleManager.getMapFromJson("{\"key1\": \"val1\", \"key2\": \"val2\"}");
        assertThat(map.keySet().size(), is(2));
        assertThat(map.get("key1"), allOf(is(notNullValue()), is(equalTo("val1"))));
        assertThat(map.get("key2"), allOf(is(notNullValue()), is(equalTo("val2"))));
    }

}
