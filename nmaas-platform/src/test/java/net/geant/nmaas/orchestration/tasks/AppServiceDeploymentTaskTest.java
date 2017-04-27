package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerPortForwarding;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.entities.DockerContainerTemplate;
import net.geant.nmaas.orchestration.entities.Identifier;
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
public class AppServiceDeploymentTaskTest {

    @Autowired
    private ApplicationRepository applications;

    @Autowired
    private AppRequestVerificationTask task;

    private Identifier clientId;

    private Identifier applicationId;

    @Before
    public void setup() {
        clientId = Identifier.newInstance(String.valueOf(100L));
        Application application = new Application("testOxidized");
        application.setDockerContainerTemplate(oxidizedTemplate());
        application = applications.save(application);
        assertThat(application.getId(), is(notNullValue()));
        applicationId = Identifier.newInstance(String.valueOf(application.getId()));
    }

    @Test
    public void shouldBuildServiceName() {
        assertThat(task.buildDcnName(clientId), containsString(clientId.value()));
    }

    @Test
    public void shouldRetrieveTemplate() throws InvalidApplicationIdException {
        assertThat(task.template(applicationId), equalTo(oxidizedTemplate()));
    }

    @Test
    public void getLongFromIdentifier() {
        assertTrue(10L == Long.valueOf(Identifier.newInstance("10").getValue()));
    }

    private DockerContainerTemplate oxidizedTemplate() {
        DockerContainerTemplate oxidizedTemplate = new DockerContainerTemplate("oxidized/oxidized:latest");
        oxidizedTemplate.setEnvVariables(Arrays.asList("CONFIG_RELOAD_INTERVAL=600"));
        oxidizedTemplate.setExposedPort(new DockerContainerPortForwarding(DockerContainerPortForwarding.Protocol.TCP, 8888));
        oxidizedTemplate.setContainerVolumes(Arrays.asList("/root/.config/oxidized"));
        return oxidizedTemplate;
    }
}
