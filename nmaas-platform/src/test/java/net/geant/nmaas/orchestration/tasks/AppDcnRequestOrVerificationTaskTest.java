package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.nmservice.NmServiceDeploymentStateChangeEvent;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppRequestNewOrVerifyExistingDcnEvent;
import net.geant.nmaas.orchestration.events.dcn.DcnVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-engine.properties")
public class AppDcnRequestOrVerificationTaskTest {

    @Autowired
    private ApplicationRepository applications;

    @Autowired
    private AppDeploymentRepositoryManager deployments;

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;

    @Autowired
    private AppDcnRequestOrVerificationTask task;

    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    private AppRequestNewOrVerifyExistingDcnEvent event = new AppRequestNewOrVerifyExistingDcnEvent(this, deploymentId);

    private Identifier clientId = Identifier.newInstance("clientId");

    @Before
    public void setup() {
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setSupportedDeploymentEnvironments(Arrays.asList(AppDeploymentEnv.DOCKER_ENGINE));
        appDeploymentSpec.setDockerContainerTemplate(oxidizedTemplate());
        Application application = new Application("testOxidized");
        application.setAppDeploymentSpec(appDeploymentSpec);
        application = applications.save(application);
        AppDeployment appDeployment = new AppDeployment();
        appDeployment.setDeploymentId(deploymentId);
        appDeployment.setApplicationId(Identifier.newInstance(String.valueOf(application.getId())));
        appDeployment.setClientId(clientId);
        deployments.store(appDeployment);
    }

    @After
    public void cleanup() throws InvalidClientIdException {
        applications.deleteAll();
        deployments.removeAll();
        dcnRepositoryManager.removeDcnInfo(clientId);
    }

    @Test
    public void shouldGenerateNewDcnDeploymentAction() throws InvalidDeploymentIdException, InvalidClientIdException {
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent instanceof DcnVerifyRequestActionEvent, is(true));
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(new DcnSpec("dcn", clientId)));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, clientId, DcnDeploymentState.REMOVED));
        resultEvent = task.trigger(event);
        assertThat(resultEvent instanceof DcnVerifyRequestActionEvent, is(true));
    }

    @Test
    public void shouldNotifyReadyForDeploymentState() throws InvalidDeploymentIdException, InvalidClientIdException {
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(new DcnSpec("dcn", clientId)));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, clientId, DcnDeploymentState.VERIFIED));
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent instanceof NmServiceDeploymentStateChangeEvent, is(true));
    }

    @Test
    public void shouldNotGenerateAnyAction() throws InvalidDeploymentIdException, InvalidClientIdException {
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(new DcnSpec("dcn", clientId)));
        ApplicationEvent resultEvent = task.trigger(event);
        assertThat(resultEvent, is(nullValue()));
    }

    private DockerContainerTemplate oxidizedTemplate() {
        DockerContainerTemplate oxidizedTemplate = new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
        return oxidizedTemplate;
    }

}
