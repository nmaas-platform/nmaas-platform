package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.projections.AppDeploymentCount;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class AppDeploymentRepositoryTest {

    @Autowired
    private AppDeploymentRepository repository;

    @Autowired
    private DomainRepository domainRepository;

    private static final String DOMAIN = "domain1";
    private static final String DOMAIN_CODENAME = "cod-dom-1";
    private static final String DEPLOYMENT_NAME_1 = "deploymentName1";
    private static final String DEPLOYMENT_NAME_2 = "deploymentName2";
    private Identifier deploymentId1 = Identifier.newInstance("deploymentId1");
    private Identifier deploymentId2 = Identifier.newInstance("deploymentId2");
    private Identifier applicationId = Identifier.newInstance("applicationId");

    @AfterEach
    public void tearDown() {
        repository.deleteAll();
        domainRepository.deleteAll();
    }

    @Test
    public void shouldAddUpdateAndRemoveAppDeployment() {
        AppDeployment appDeployment = new AppDeployment();
        appDeployment.setDeploymentId(deploymentId1);
        appDeployment.setApplicationId(applicationId);
        appDeployment.setDomain(DOMAIN_CODENAME);
        appDeployment.setDeploymentName(DEPLOYMENT_NAME_1);
        AppDeployment storedAppDeployment = repository.save(appDeployment);
        assertThat(storedAppDeployment.getId(), is(notNullValue()));
        appDeployment = repository.findById(storedAppDeployment.getId()).get();
        appDeployment.setConfiguration(new AppConfiguration("configuration-string"));
        repository.save(appDeployment);
        assertThat(repository.count(), equalTo(1L));
        assertThat(repository.findByDeploymentId(deploymentId1).isPresent(), is(true));
        assertThat(repository.getStateByDeploymentId(deploymentId1).get(), equalTo(AppDeploymentState.REQUESTED));
        assertThat(repository.getDomainByDeploymentId(deploymentId1).get(), equalTo(DOMAIN_CODENAME));
        assertThat(repository.findByDomainAndState(DOMAIN_CODENAME, AppDeploymentState.REQUESTED).size(), equalTo(1));
        AppDeployment appDeployment2 = new AppDeployment();
        appDeployment2.setDeploymentId(deploymentId2);
        appDeployment2.setApplicationId(applicationId);
        appDeployment2.setDomain(DOMAIN_CODENAME);
        appDeployment2.setDeploymentName(DEPLOYMENT_NAME_2);
        repository.save(appDeployment2);
        assertThat(repository.findByDomainAndState(DOMAIN_CODENAME, AppDeploymentState.REQUESTED).size(), equalTo(2));
        repository.deleteAll();
        assertThat(repository.count(), equalTo(0L));
        assertThat(repository.findByDomainAndState(DOMAIN_CODENAME, AppDeploymentState.REQUESTED).size(), equalTo(0));
    }

    @Test
    public void shouldReadDomainNameProperly() {
        AppDeployment appDeployment = new AppDeployment();
        appDeployment.setDeploymentId(deploymentId1);
        appDeployment.setApplicationId(applicationId);
        appDeployment.setDomain(DOMAIN_CODENAME);
        appDeployment.setDeploymentName(DEPLOYMENT_NAME_1);
        AppDeployment storedAppDeployment = repository.save(appDeployment);

        Domain d = new Domain(DOMAIN, DOMAIN_CODENAME);
        d = domainRepository.save(d);

        String domain_name = repository.getDomainNameByDeploymentId(storedAppDeployment.getDeploymentId()).orElse(null);

        assertThat(domain_name, equalTo(DOMAIN));
    }

    @Test
    public void shouldGetAppDeploymentCountByAppName() {
        AppDeployment appDeployment = new AppDeployment();
        appDeployment.setDeploymentId(deploymentId1);
        appDeployment.setApplicationId(applicationId);
        appDeployment.setDomain(DOMAIN_CODENAME);
        appDeployment.setDeploymentName(DEPLOYMENT_NAME_1);
        appDeployment.setAppName("Grafana");
        appDeployment.setState(AppDeploymentState.APPLICATION_DEPLOYED);
        AppDeployment storedAppDeployment = repository.save(appDeployment);

        List<AppDeploymentCount> result = repository.countAllRunningByAppName();
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCount());
        assertEquals("Grafana", result.get(0).getApplicationName());

    }

}
