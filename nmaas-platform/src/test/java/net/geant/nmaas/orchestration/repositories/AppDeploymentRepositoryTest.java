package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class AppDeploymentRepositoryTest {

    @Autowired
    private AppDeploymentRepository repository;

    private Identifier deploymentId1 = Identifier.newInstance("deploymentId1");
    private Identifier deploymentId2 = Identifier.newInstance("deploymentId2");
    private Identifier applicationId = Identifier.newInstance("applicationId");
    private Identifier clientId = Identifier.newInstance("clientId");

    @Test
    public void shouldAddUpdateAndRemoveAppDeployment() {
        AppDeployment appDeployment = new AppDeployment();
        appDeployment.setDeploymentId(deploymentId1);
        appDeployment.setApplicationId(applicationId);
        appDeployment.setClientId(clientId);
        AppDeployment storedAppDeployment = repository.save(appDeployment);
        assertThat(storedAppDeployment.getId(), is(notNullValue()));
        appDeployment = repository.findOne(storedAppDeployment.getId());
        appDeployment.setConfiguration(new AppConfiguration("configuration-string"));
        repository.save(appDeployment);
        assertThat(repository.count(), equalTo(1L));
        assertThat(repository.findByDeploymentId(deploymentId1).isPresent(), is(true));
        assertThat(repository.getStateByDeploymentId(deploymentId1).get(), equalTo(AppDeploymentState.REQUESTED));
        assertThat(repository.getClientIdByDeploymentId(deploymentId1).get(), equalTo(clientId));
        assertThat(repository.findByClientIdAndState(clientId, AppDeploymentState.REQUESTED).size(), equalTo(1));
        AppDeployment appDeployment2 = new AppDeployment();
        appDeployment2.setDeploymentId(deploymentId2);
        appDeployment2.setApplicationId(applicationId);
        appDeployment2.setClientId(clientId);
        repository.save(appDeployment2);
        assertThat(repository.findByClientIdAndState(clientId, AppDeploymentState.REQUESTED).size(), equalTo(2));
        repository.deleteAll();
        assertThat(repository.count(), equalTo(0L));
        assertThat(repository.findByClientIdAndState(clientId, AppDeploymentState.REQUESTED).size(), equalTo(0));
    }

}
