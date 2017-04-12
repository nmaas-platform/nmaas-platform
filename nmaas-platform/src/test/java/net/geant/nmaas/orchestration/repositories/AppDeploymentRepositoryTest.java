package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
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

    @Test
    public void shouldAddUpdateAndRemoveAppDeployment() {
        AppDeployment appDeployment = new AppDeployment();
        appDeployment.setDeploymentId(Identifier.newInstance("deploymentId"));
        appDeployment.setApplicationId(Identifier.newInstance("applicationId"));
        appDeployment.setClientId(Identifier.newInstance("clientId"));
        AppDeployment storedAppDeployment = repository.save(appDeployment);
        assertThat(storedAppDeployment.getId(), is(notNullValue()));
        appDeployment = repository.findOne(storedAppDeployment.getId());
        appDeployment.setConfiguration(new AppConfiguration("configuration-string"));
        repository.save(appDeployment);
        assertThat(repository.count(), equalTo(1L));
        assertThat(repository.findByDeploymentId(Identifier.newInstance("deploymentId")).isPresent(), is(true));
        repository.delete(storedAppDeployment.getId());
        assertThat(repository.count(), equalTo(0L));
    }

}
