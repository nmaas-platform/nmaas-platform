package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppDeploymentRepositoryManagerIntTest {

    @Autowired
    private AppDeploymentRepositoryManager repositoryManager;

    @Autowired
    private AppDeploymentRepository repository;

    private static final String DOMAIN = "domain1";
    private static final String DEPLOYMENT_NAME_1 = "deploymentName1";
    private Identifier deploymentId1 = Identifier.newInstance("deploymentId1");
    private Identifier applicationId = Identifier.newInstance("applicationId");

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @Test
    public void shouldStoreAndUpdateAppDeployment() throws InvalidDeploymentIdException {
        AppDeployment appDeployment = AppDeployment.builder()
                .deploymentId(deploymentId1)
                .applicationId(applicationId)
                .domain(DOMAIN)
                .deploymentName(DEPLOYMENT_NAME_1)
                .build();

        repositoryManager.store(appDeployment);
        assertThat(repositoryManager.load(deploymentId1), is(notNullValue()));
        assertThat(repositoryManager.loadState(deploymentId1), is(AppDeploymentState.REQUESTED));
        assertThat(repositoryManager.loadDomain(deploymentId1), is(DOMAIN));
        assertThat(repositoryManager.loadAllWaitingForDcn(DOMAIN).size(), is(0));

        repositoryManager.updateState(deploymentId1, AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED);
        assertThat(repositoryManager.loadState(deploymentId1), is(AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED));
        assertThat(repositoryManager.loadAllWaitingForDcn(DOMAIN).size(), is(1));
    }

}
