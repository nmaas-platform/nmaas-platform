package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DefaultAppDeploymentRepositoryManagerIntTest {

    @Autowired
    private DefaultAppDeploymentRepositoryManager repositoryManager;

    @Autowired
    private AppDeploymentRepository repository;

    private static final String DOMAIN = "domain1";
    private static final String DEPLOYMENT_NAME_1 = "deploymentName1";
    private Identifier deploymentId1 = Identifier.newInstance("deploymentId1");
    private Identifier applicationId = Identifier.newInstance("applicationId");

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void shouldStoreAndUpdateAppDeployment() throws InvalidDeploymentIdException {
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
