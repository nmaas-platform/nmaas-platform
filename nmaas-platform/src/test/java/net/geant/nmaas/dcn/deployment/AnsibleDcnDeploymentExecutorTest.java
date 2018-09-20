package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class AnsibleDcnDeploymentExecutorTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private DcnInfoRepository dcnInfoRepository;

    private AnsibleDcnDeploymentExecutor executor;

    @Before
    public void setup() {
        executor = new AnsibleDcnDeploymentExecutor(
                dcnRepositoryManager,
                null,
                null,
                null,
                null,
                null);
    }

    @After
    public void clean() {
        dcnInfoRepository.deleteAll();
    }

    @Test
    public void shouldReturnProperErrorState() {
        assertThat(executor.deploymentOrRemovalFailureDependingOnLastState(DcnDeploymentState.DEPLOYMENT_INITIATED),
                equalTo(DcnDeploymentState.DEPLOYMENT_FAILED));
        assertThat(executor.deploymentOrRemovalFailureDependingOnLastState(DcnDeploymentState.REMOVAL_INITIATED),
                equalTo(DcnDeploymentState.REMOVAL_FAILED));
        assertThat(executor.deploymentOrRemovalFailureDependingOnLastState(DcnDeploymentState.DEPLOYED),
                equalTo(DcnDeploymentState.ERROR));
    }

    @Test
    public void shouldCheckCurrentDcnState() throws InvalidDomainException {
        String domain = "domain";
        DcnInfo dcnInfo = new DcnInfo();
        dcnInfo.setDomain("domain2");
        dcnInfo.setName("name");
        dcnRepositoryManager.storeDcnInfo(dcnInfo);
        assertThat(executor.checkState(domain), equalTo(DcnState.NONE));
        dcnInfo.setDomain(domain);
        dcnInfo.setName("name2");
        dcnRepositoryManager.storeDcnInfo(dcnInfo);
        assertThat(executor.checkState(domain), equalTo(DcnState.NONE));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, domain, DcnDeploymentState.VERIFIED));
        assertThat(executor.checkState(domain), equalTo(DcnState.DEPLOYED));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, domain, DcnDeploymentState.REMOVED));
        assertThat(executor.checkState(domain), equalTo(DcnState.REMOVED));
    }

}
