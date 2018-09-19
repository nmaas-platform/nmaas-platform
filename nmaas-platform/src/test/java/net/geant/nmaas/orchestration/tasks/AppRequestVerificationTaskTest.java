package net.geant.nmaas.orchestration.tasks;

import net.geant.nmaas.nmservice.deployment.exceptions.NmServiceRequestVerificationException;
import net.geant.nmaas.orchestration.AppDeploymentRepositoryManager;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentEnv;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.events.app.AppVerifyRequestActionEvent;
import net.geant.nmaas.orchestration.exceptions.InvalidApplicationIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.tasks.app.AppRequestVerificationTask;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppRequestVerificationTaskTest {

    @Autowired
    private ApplicationRepository applications;
    @Autowired
    private AppDeploymentRepositoryManager deployments;
    @Autowired
    private AppRequestVerificationTask task;

    private static final String DOMAIN = "domain1";
    private static final String DEPLOYMENT_NAME = "deploymentName";
    private Identifier deploymentId = Identifier.newInstance("deploymentId");

    @Before
    public void setup() {
        AppDeploymentSpec appDeploymentSpec = new AppDeploymentSpec();
        appDeploymentSpec.setDefaultStorageSpace(20.0);
        appDeploymentSpec.setSupportedDeploymentEnvironments(Arrays.asList(AppDeploymentEnv.DOCKER_COMPOSE));
        Application application = new Application("testOxidized");
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
    public void cleanup() {
        applications.deleteAll();
        deployments.removeAll();
    }

    @Test
    public void shouldNotifyServiceVerificationProblemSinceDeploymentEnvironmentNotSupported() throws InvalidApplicationIdException, InvalidDeploymentIdException, InterruptedException {
        try {
            task.trigger(new AppVerifyRequestActionEvent(this, deploymentId));
        } catch (NmServiceRequestVerificationException e) {}
        Thread.sleep(200);
        assertThat(deployments.loadState(deploymentId), equalTo(AppDeploymentState.REQUEST_VALIDATION_FAILED));
    }

    @Test
    public void getLongFromIdentifier() {
        assertTrue(10L == Long.valueOf(Identifier.newInstance("10").getValue()));
    }

}
