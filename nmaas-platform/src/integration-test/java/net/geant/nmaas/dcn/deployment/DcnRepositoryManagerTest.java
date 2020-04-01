package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DcnRepositoryManagerTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private DcnInfoRepository dcnInfoRepository;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    private static Identifier DEPLOYMENT_ID = Identifier.newInstance("did");
    private static String DOMAIN = "domain";
    private static String DEPLOYMENT_NAME = "deploymentName";

    @BeforeEach
    public void populateRepositories() {
        AppDeployment appDeployment = AppDeployment.builder().deploymentId(DEPLOYMENT_ID)
                .domain(DOMAIN)
                .applicationId(Identifier.newInstance(""))
                .deploymentName(DEPLOYMENT_NAME)
                .configFileRepositoryRequired(true)
                .storageSpace(20)
                .build();
        appDeploymentRepository.save(appDeployment);
    }

    @AfterEach
    public void cleanRepositories() {
        appDeploymentRepository.deleteAll();
        dcnInfoRepository.deleteAll();
    }

    @Test
    public void shouldAddUpdateAndRemoteDcns() throws InvalidDomainException {
        storeDefaultDcnInfoInRepository();
        assertThat(dcnRepositoryManager.loadCurrentState(DOMAIN), equalTo(DcnDeploymentState.INIT));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, DOMAIN, DcnDeploymentState.REQUEST_VERIFIED));
        assertThat(dcnRepositoryManager.loadCurrentState(DOMAIN), equalTo(DcnDeploymentState.REQUEST_VERIFIED));
        assertThat(dcnRepositoryManager.loadNetwork(DOMAIN), is(notNullValue()));
        dcnRepositoryManager.removeDcnInfo(DOMAIN);
        assertThat(dcnInfoRepository.count(), equalTo(0L));
    }

    @Test
    public void shouldThrowExceptionOnMissingDeployment(){
        assertThrows(InvalidDomainException.class, () -> {
            appDeploymentRepository.deleteAll();
            dcnRepositoryManager.loadNetwork(DOMAIN);
        });
    }

    @Test
    public void shouldThrowExceptionOnMissingDcnForGivenClient(){
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.loadNetwork(DOMAIN);
        });
    }

    @Test
    public void shouldThrowExceptionDuringRemovalOnMissingDcnForGivenClient() {
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.removeDcnInfo(DOMAIN);
        });
    }

    @Test
    public void shouldThrowExceptionDuringStateNotificationOnMissingDcnForGivenClient() {
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, DOMAIN, DcnDeploymentState.DEPLOYMENT_INITIATED));
        });
    }

    @Test
    public void shouldThrowExceptionDuringStateRetrievalOnMissingDcnForGivenClient() {
        assertThrows(InvalidDomainException.class, () -> {
            dcnRepositoryManager.loadCurrentState(DOMAIN);
        });
    }

    private void storeDefaultDcnInfoInRepository() throws InvalidDomainException {
        DcnSpec spec = new DcnSpec("", DOMAIN, DcnDeploymentType.NONE);
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(spec));
    }
}
