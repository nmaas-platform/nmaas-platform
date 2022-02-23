package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.events.app.AppUpgradeActionEvent;
import net.geant.nmaas.portal.api.domain.AppInstanceView;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AppUpgradeTriggerServiceTest {

    private static final Identifier DEPLOYMENT_ID1 = Identifier.newInstance(10L);
    private static final Identifier DEPLOYMENT_ID2 = Identifier.newInstance(20L);
    private static final Identifier DEPLOYMENT_ID3 = Identifier.newInstance(30L);
    private static final Identifier DEPLOYMENT_ID4 = Identifier.newInstance(40L);
    private static final Long APPLICATION_ID1 = 1L;
    private static final Long APPLICATION_ID2 = 2L;
    private static final Long APPLICATION_ID3 = 3L;
    private static final Long APPINSTANCE_ID1 = 11L;
    private static final Long APPINSTANCE_ID2 = 12L;
    private static final Long APPINSTANCE_ID3 = 13L;

    private static final Application APPLICATION1 = new Application(APPLICATION_ID1,"appname1", "appversion1");
    private static final Application APPLICATION2 = new Application(APPLICATION_ID2,"appname2", "appversion2");
    private static final Domain DOMAIN = new Domain("Domain", "domain");

    private static final AppInstance APP_INSTANCE1 = new AppInstance(APPINSTANCE_ID1, APPLICATION1, DOMAIN, "appinstance1", false);
    private static final AppInstance APP_INSTANCE2 = new AppInstance(APPINSTANCE_ID2, APPLICATION1, DOMAIN, "appinstance2", true);
    private static final AppInstance APP_INSTANCE3 = new AppInstance(APPINSTANCE_ID3, APPLICATION2, DOMAIN, "appinstance3", true);

    private final AppDeploymentRepositoryManager appDeploymentRepositoryManager = mock(AppDeploymentRepositoryManager.class);
    private final ApplicationInstanceService applicationInstanceService = mock(ApplicationInstanceService.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

    private final AppUpgradeTriggerService service = new AppUpgradeTriggerService(appDeploymentRepositoryManager, applicationInstanceService, applicationEventPublisher);

    @BeforeAll
    static void init() {
        APP_INSTANCE1.setInternalId(DEPLOYMENT_ID2);
        APP_INSTANCE2.setInternalId(DEPLOYMENT_ID3);
        APP_INSTANCE3.setInternalId(DEPLOYMENT_ID4);
    }

    @Test
    void shouldTriggerAutomaticUpgrade() throws JobExecutionException {
        when(appDeploymentRepositoryManager.loadByState(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED)).thenReturn(
                Arrays.asList(
                        AppDeployment.builder().deploymentId(DEPLOYMENT_ID1).build(),
                        AppDeployment.builder().deploymentId(DEPLOYMENT_ID2).build(),
                        AppDeployment.builder().deploymentId(DEPLOYMENT_ID3).build(),
                        AppDeployment.builder().deploymentId(DEPLOYMENT_ID4).build()
                )
        );
        when(applicationInstanceService.findByInternalId(DEPLOYMENT_ID1)).thenReturn(Optional.empty());
        when(applicationInstanceService.findByInternalId(DEPLOYMENT_ID2)).thenReturn(Optional.of(APP_INSTANCE1));
        when(applicationInstanceService.findByInternalId(DEPLOYMENT_ID3)).thenReturn(Optional.of(APP_INSTANCE2));
        when(applicationInstanceService.findByInternalId(DEPLOYMENT_ID4)).thenReturn(Optional.of(APP_INSTANCE3));
        when(applicationInstanceService.checkUpgradePossible(APPINSTANCE_ID1)).thenReturn(Boolean.TRUE);
        when(applicationInstanceService.checkUpgradePossible(APPINSTANCE_ID2)).thenReturn(Boolean.TRUE);
        when(applicationInstanceService.checkUpgradePossible(APPINSTANCE_ID3)).thenReturn(Boolean.FALSE);
        when(applicationInstanceService.obtainUpgradeInfo(APP_INSTANCE1.getId())).thenReturn(new AppInstanceView.AppInstanceUpgradeInfo(APPLICATION_ID3, "", ""));
        when(applicationInstanceService.obtainUpgradeInfo(APP_INSTANCE2.getId())).thenReturn(new AppInstanceView.AppInstanceUpgradeInfo(APPLICATION_ID3, "", ""));

        service.trigger();

        ArgumentCaptor<AppUpgradeActionEvent> appUpgradeActionEventArgumentCaptor = ArgumentCaptor.forClass(AppUpgradeActionEvent.class);
        verify(applicationEventPublisher).publishEvent(appUpgradeActionEventArgumentCaptor.capture());
        AppUpgradeActionEvent result = appUpgradeActionEventArgumentCaptor.getValue();
        assertThat(result.getApplicationId()).isEqualTo(Identifier.newInstance(APPLICATION_ID3));
        assertThat(result.getRelatedTo()).isEqualTo(DEPLOYMENT_ID3);
        assertThat(result.getAppUpgradeMode()).isEqualTo(AppUpgradeMode.AUTO);
    }

}
