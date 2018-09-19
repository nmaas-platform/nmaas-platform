package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"env_docker-compose", "dcn_manual"})
public class ManualDcnDeploymentExecutorWorkflowIntTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @MockBean
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private DomainService domainService;

    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("did");
    private static final String DOMAIN = "domain";

    @Test
    public void shouldProceedDcnWorkflowToWaitingForOperatorState() throws Exception {
        when(appDeploymentRepositoryManager.loadDomainByDeploymentId(DEPLOYMENT_ID)).thenReturn(DOMAIN);
        domainService.storeDcnInfo(DOMAIN);
        eventPublisher.publishEvent(new AppRequestNewOrVerifyExistingDcnEvent(this, DEPLOYMENT_ID));
        Thread.sleep(500);
        assertThat(dcnRepositoryManager.loadCurrentState(DOMAIN), is(DcnDeploymentState.WAITING_FOR_OPERATOR_CONFIRMATION));
        dcnRepositoryManager.removeDcnInfo(DOMAIN);
    }

}
