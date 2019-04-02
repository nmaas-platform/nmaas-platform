package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.orchestration.DefaultAppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.portal.api.domain.DomainDcnDetailsView;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"env_kubernetes", "db_memory"})
public class ManualDcnDeploymentExecutorWorkflowIntTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private DefaultAppDeploymentRepositoryManager appDeploymentRepositoryManager;

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;

    @Autowired
    private DomainService domainService;

    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("did");
    private static final String DOMAIN = "domain";

    @BeforeEach
    public void setup(){
        DomainRequest domainRequest = new DomainRequest(DOMAIN, DOMAIN, true);
        domainRequest.setDomainDcnDetailsView(new DomainDcnDetailsView(null, DOMAIN, false, DcnDeploymentType.MANUAL));
        domainService.createDomain(domainRequest);
    }

    @Test
    public void shouldProceedDcnWorkflowToWaitingForOperatorState() throws Exception {
        when(appDeploymentRepositoryManager.loadDomain(DEPLOYMENT_ID)).thenReturn(DOMAIN);
        domainService.storeDcnInfo(DOMAIN, DcnDeploymentType.MANUAL);
        eventPublisher.publishEvent(new AppRequestNewOrVerifyExistingDcnEvent(this, DEPLOYMENT_ID));
        Thread.sleep(500);
        assertThat(dcnRepositoryManager.loadCurrentState(DOMAIN), is(DcnDeploymentState.WAITING_FOR_OPERATOR_CONFIRMATION));
        dcnRepositoryManager.removeDcnInfo(DOMAIN);
    }

}
