package net.geant.nmaas.orchestration;

import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppDeploymentRepositoryManagerIntTest {

    @Autowired
    private AppDeploymentRepositoryManager repositoryManager;

    private static final String DOMAIN = "domain1";
    private static final String DEPLOYMENT_NAME_1 = "deploymentName1";
    private static final String DEPLOYMENT_NAME_2 = "deploymentName2";
    private Identifier deploymentId1 = Identifier.newInstance("deploymentId1");
    private Identifier deploymentId2 = Identifier.newInstance("deploymentId2");
    private Identifier applicationId = Identifier.newInstance("applicationId");

    @Test
    public void shouldAddUpdateAndRemoveAppDeployment() throws InvalidDeploymentIdException {
        AppDeployment appDeployment = new AppDeployment();
        appDeployment.setDeploymentId(deploymentId1);
        appDeployment.setApplicationId(applicationId);
        appDeployment.setDomain(DOMAIN);
        appDeployment.setDeploymentName(DEPLOYMENT_NAME_1);
        repositoryManager.store(appDeployment);
        assertThat(repositoryManager.load(deploymentId1).isPresent(), is(true));
        assertThat(repositoryManager.loadState(deploymentId1), equalTo(AppDeploymentState.REQUESTED));
        repositoryManager.updateState(deploymentId1, AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED);
        repositoryManager.updateConfiguration(deploymentId1, new AppConfiguration("configuration-string"));
        assertThat(repositoryManager.loadState(deploymentId1), equalTo(AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED));
        assertThat(repositoryManager.loadDomainByDeploymentId(deploymentId1), equalTo(DOMAIN));
        assertThat(repositoryManager.loadAllWaitingForDcn(DOMAIN).size(), equalTo(1));
        AppDeployment appDeployment2 = new AppDeployment();
        appDeployment2.setDeploymentId(deploymentId2);
        appDeployment2.setApplicationId(applicationId);
        appDeployment2.setDomain(DOMAIN);
        appDeployment2.setDeploymentName(DEPLOYMENT_NAME_2);
        appDeployment2.setState(AppDeploymentState.DEPLOYMENT_ENVIRONMENT_PREPARED);
        repositoryManager.store(appDeployment2);
        assertThat(repositoryManager.loadAllWaitingForDcn(DOMAIN).size(), equalTo(2));
        repositoryManager.removeAll();
        assertThat(repositoryManager.loadAllWaitingForDcn(DOMAIN).size(), equalTo(0));
    }

}
