package net.geant.nmaas.orchestration.task;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerTemplate;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class AppDeploymentOrchestratorTaskTest {

    private AppDeploymentOrchestratorTaskHelper taskHelper;

    @Autowired
    private ApplicationRepository applications;

    private Identifier clientId;

    private Identifier applicationId;

    @Before
    public void setup() {
        taskHelper = new AppDeploymentOrchestratorTaskHelper(applications);
        clientId = Identifier.newInstance(String.valueOf(100L));
        Application application = new Application("testOxidized");
        application.setDockerContainerTemplate(oxidizedTemplate());
        application = applications.save(application);
        assertThat(application.getId(), is(notNullValue()));
        applicationId = Identifier.newInstance(String.valueOf(application.getId()));
    }

    @Test
    public void shouldConstructServiceInfo() throws InvalidApplicationIdException {
        DockerContainerSpec spec = (DockerContainerSpec) taskHelper.constructNmServiceSpec(clientId, applicationId);
        assertThat(spec.getTemplate(), is(notNullValue()));
        spec.getTemplate().setId(null);
        spec.getTemplate().getExposedPort().setId(null);
        assertThat(spec.getTemplate(), equalTo(oxidizedTemplate()));
    }

    @Test
    public void shouldBuildServiceName() {
        assertThat(taskHelper.buildServiceName(applications.findOne(Long.valueOf(applicationId.getValue()))),
                equalTo("testOxidized" + "-" + applicationId));
    }

    @Test
    public void getLongFromIdentifier() {
        assertTrue(10L == Long.valueOf(Identifier.newInstance("10").getValue()));
    }

    private DockerContainerTemplate oxidizedTemplate() {
        DockerContainerTemplate oxidizedTemplate =
                new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setEnvVariablesInSpecRequired(false);
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
        return oxidizedTemplate;
    }
}
