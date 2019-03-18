package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnState;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AnsibleDcnDeploymentExecutorTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;

    @Autowired
    private DcnInfoRepository dcnInfoRepository;

    private AnsibleDcnDeploymentExecutor executor;

    @BeforeEach
    public void setup() {
        executor = new AnsibleDcnDeploymentExecutor(
                dcnRepositoryManager,
                null,
                null,
                null,
                null
        );
    }

    @AfterEach
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
