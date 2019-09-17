package net.geant.nmaas.dcn.deployment;

import java.util.Collections;
import net.geant.nmaas.orchestration.DefaultAppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.portal.api.domain.DomainDcnDetailsView;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"env_kubernetes", "db_memory"})
public class DummyDcnDeploymentExecutorWorkflowIntTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private DefaultAppDeploymentRepositoryManager appDeploymentRepositoryManager;

    @MockBean
    private DcnRepositoryManager dcnRepositoryManager;

    @Autowired
    private DomainService domainService;

    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("did");
    private static final String DOMAIN = "domain";

    @BeforeEach
    public void setup(){
        DomainRequest domainRequest = new DomainRequest(DOMAIN, DOMAIN, true);
        domainRequest.setDomainDcnDetails(new DomainDcnDetailsView(null, DOMAIN, false, DcnDeploymentType.NONE, Collections.emptyList()));
        domainService.createDomain(domainRequest);
    }

    @Test
    public void shouldCompleteDcnWorkflowWithDummyExecutor() {
        when(appDeploymentRepositoryManager.loadDomain(DEPLOYMENT_ID)).thenReturn(DOMAIN);
        when(dcnRepositoryManager.loadCurrentState(DOMAIN)).thenThrow(new InvalidDomainException());
        when(dcnRepositoryManager.loadType(any())).thenReturn(DcnDeploymentType.NONE);
        eventPublisher.publishEvent(new AppRequestNewOrVerifyExistingDcnEvent(this, DEPLOYMENT_ID));
        verify(appDeploymentRepositoryManager, timeout(1000)).loadAllWaitingForDcn(DOMAIN);
    }

    @AfterEach
    public void tearDown(){
        domainService.getDomains().stream()
                .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                .forEach(domain -> domainService.removeDomain(domain.getId()));
    }

}
