package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"env_docker-engine", "dcn_manual", "conf_repo"})
public class ManualDcnDeploymentExecutorWorkflowTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @MockBean
    private AppDeploymentRepositoryManager appDeploymentRepositoryManager;
    @MockBean
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private DomainRepository domainRepository;

    private static final Identifier DEPLOYMENT_ID = Identifier.newInstance("did");
    private static final String DOMAIN = "domaintest";

    @Test
    public void shouldCompleteDcnWorkflowWithManualExecutor() throws Exception {
        Domain domain = new Domain(DOMAIN, DOMAIN, true, "namespace", true);
        domainRepository.save(domain);
        when(appDeploymentRepositoryManager.loadDomainByDeploymentId(any())).thenReturn(DOMAIN);
        when(dcnRepositoryManager.loadCurrentState(DOMAIN)).thenThrow(new InvalidDomainException());
        when(dcnRepositoryManager.exists(DOMAIN)).thenReturn(true);
        eventPublisher.publishEvent(new AppRequestNewOrVerifyExistingDcnEvent(this, DEPLOYMENT_ID));
        verify(appDeploymentRepositoryManager, timeout(500)).loadAllWaitingForDcn(DOMAIN);
    }

}
