package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.projections.AppDeploymentCount;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AppDeploymentRepositoryIntTest {

    private static final String DOMAIN = "domain1";
    private static final String DOMAIN_CODENAME = "cod-dom-1";
    private static final String DEPLOYMENT_NAME_1 = "deploymentName1";
    private static final String DEPLOYMENT_NAME_2 = "deploymentName2";
    private static final Identifier DEPLOYMENT_ID_1 = Identifier.newInstance("deploymentId1");
    private static final Identifier DEPLOYMENT_ID_2 = Identifier.newInstance("deploymentId2");
    private static final Identifier APPLICATION_ID = Identifier.newInstance("applicationId");

    @Autowired
    private AppDeploymentRepository repository;

    @Autowired
    private DomainRepository domainRepository;

    @AfterEach
    void cleanRepositories() {
        repository.deleteAll();
        domainRepository.deleteAll();
    }

    @Test
    void shouldAddUpdateAndRemoveAppDeployment() {
        AppDeployment appDeployment = getDefaultAppDeployment();
        AppDeployment storedAppDeployment = repository.save(appDeployment);
        assertThat(storedAppDeployment.getId()).isNotNull();

        appDeployment = repository.findById(storedAppDeployment.getId()).get();
        appDeployment.setConfiguration(new AppConfiguration("configuration-string"));

        repository.save(appDeployment);

        assertThat(repository.count()).isOne();
        assertThat(repository.findByDeploymentId(DEPLOYMENT_ID_1)).isPresent();
        assertThat(repository.getStateByDeploymentId(DEPLOYMENT_ID_1).get()).isEqualTo(AppDeploymentState.REQUESTED);
        assertThat(repository.getDomainByDeploymentId(DEPLOYMENT_ID_1).get()).isEqualTo(DOMAIN_CODENAME);
        assertThat(repository.findByDomainAndState(DOMAIN_CODENAME, AppDeploymentState.REQUESTED).size()).isOne();

        AppDeployment appDeployment2 = new AppDeployment();
        appDeployment2.setDeploymentId(DEPLOYMENT_ID_2);
        appDeployment2.setDescriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId2"));
        appDeployment2.setApplicationId(APPLICATION_ID);
        appDeployment2.setDomain(DOMAIN_CODENAME);
        appDeployment2.setDeploymentName(DEPLOYMENT_NAME_2);

        repository.save(appDeployment2);
        assertThat(repository.findByDomainAndState(DOMAIN_CODENAME, AppDeploymentState.REQUESTED).size()).isEqualTo(2);

        repository.deleteAll();
        assertThat(repository.count()).isZero();
        assertThat(repository.findByDomainAndState(DOMAIN_CODENAME, AppDeploymentState.REQUESTED).size()).isZero();
    }

    @Test
    void shouldReadDomainNameProperly() {
        AppDeployment appDeployment = getDefaultAppDeployment();
        AppDeployment storedAppDeployment = repository.save(appDeployment);
        Domain d = new Domain(DOMAIN, DOMAIN_CODENAME);
        domainRepository.save(d);

        String domainName = repository.getDomainNameByDeploymentId(storedAppDeployment.getDeploymentId()).orElse(null);

        assertThat(domainName).isEqualTo(DOMAIN);
    }

    @Test
    void shouldGetAppDeploymentCountByAppName() {
        AppDeployment appDeployment = getDefaultAppDeployment();
        appDeployment.setAppName("Grafana");
        appDeployment.setState(AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        repository.save(appDeployment);

        List<AppDeploymentCount> result = repository.countAllRunningByAppName();

        assertThat(result.size()).isOne();
        assertThat(result.get(0).getCount()).isOne();
        assertThat(result.get(0).getApplicationName()).isEqualTo("Grafana");
    }

    private AppDeployment getDefaultAppDeployment() {
        AppDeployment appDeployment = new AppDeployment();
        appDeployment.setDeploymentId(DEPLOYMENT_ID_1);
        appDeployment.setDescriptiveDeploymentId(Identifier.newInstance("descriptiveDeploymentId"));
        appDeployment.setApplicationId(APPLICATION_ID);
        appDeployment.setDomain(DOMAIN_CODENAME);
        appDeployment.setDeploymentName(DEPLOYMENT_NAME_1);
        return appDeployment;
    }

}
