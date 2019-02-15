package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.orchestration.tasks.app.AppDcnRequestOrVerificationTask;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppDcnRequestOrVerificationTaskTest {

    @Autowired
    private ApplicationRepository applications;
    @Autowired
    private AppDeploymentRepositoryManager deployments;
    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private DcnInfoRepository dcnInfoRepository;
    @Autowired
    private AppDcnRequestOrVerificationTask task;

    private static final String DOMAIN = "domain";
    private static final String DEPLOYMENT_NAME = "deploymentName";
    private Identifier deploymentId = Identifier.newInstance("deploymentId");
    private AppRequestNewOrVerifyExistingDcnEvent event = new AppRequestNewOrVerifyExistingDcnEvent(this, deploymentId);

    @Before
    public void setup() {
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setSupportedDeploymentEnvironments(Arrays.asList(AppDeploymentEnv.KUBERNETES));
        appDeploymentSpec.setDefaultStorageSpace(20);
        Application application = new Application("testOxidized", "testVersion");
        application.setAppDeploymentSpec(appDeploymentSpec);
        application = applications.save(application);
        AppDeployment appDeployment = new AppDeployment();
        appDeployment.setDeploymentId(deploymentId);
        appDeployment.setApplicationId(Identifier.newInstance(String.valueOf(application.getId())));
        appDeployment.setDomain(DOMAIN);
        appDeployment.setDeploymentName(DEPLOYMENT_NAME);
        deployments.store(appDeployment);
    }

    @After
    public void cleanup() throws InvalidDomainException {
        applications.deleteAll();
        deployments.removeAll();
        dcnInfoRepository.deleteAll();
    }

    @Test
    public void shouldGenerateNewDcnDeploymentAction() throws InvalidDeploymentIdException, InvalidDomainException {
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent instanceof DcnVerifyRequestActionEvent, is(true));
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(new DcnSpec("dcn", DOMAIN)));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, DOMAIN, DcnDeploymentState.REMOVED));
        resultEvent = task.trigger(event);
        assertThat(resultEvent instanceof DcnVerifyRequestActionEvent, is(true));
    }

    @Test
    public void shouldNotifyReadyForDeploymentState() throws InvalidDeploymentIdException, InvalidDomainException {
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(new DcnSpec("dcn", DOMAIN)));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, DOMAIN, DcnDeploymentState.VERIFIED));
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent instanceof NmServiceDeploymentStateChangeEvent, is(true));
    }

}
