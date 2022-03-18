package net.geant.nmaas.orchestration.repositories;

import net.geant.nmaas.orchestration.AppUpgradeStatus;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppUpgradeHistory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AppUpgradeHistoryRepositoryIntTest {

    private static final Identifier DEPLOYMENT_ID1 = Identifier.newInstance(1L);
    private static final Identifier DEPLOYMENT_ID2 = Identifier.newInstance(2L);

    @Autowired
    private AppUpgradeHistoryRepository repository;

    @AfterEach
    public void cleanUpRepo() {
        repository.deleteAll();
    }

    @Test
    public void shouldFindHistoryEntryByDeploymentId() {
        repository.save(AppUpgradeHistory.builder()
                .deploymentId(DEPLOYMENT_ID1)
                .timestamp(Date.from(Instant.now()))
                .previousApplicationId(Identifier.newInstance(10L))
                .targetApplicationId(Identifier.newInstance(11L))
                .status(AppUpgradeStatus.SUCCESS).build());

        assertThat(repository.findByDeploymentId(DEPLOYMENT_ID1).size()).isEqualTo(1L);
        assertThat(repository.findByDeploymentId(DEPLOYMENT_ID1).get(0).getTargetApplicationId()).isEqualTo(Identifier.newInstance(11L));
    }

    @Test
    public void shouldFindHistoryEntriesFromPeriod() {
        Instant now = Instant.now();
        Date base = Date.from(now);
        repository.save(AppUpgradeHistory.builder()
                .deploymentId(DEPLOYMENT_ID1)
                .timestamp(base)
                .previousApplicationId(Identifier.newInstance(10L))
                .targetApplicationId(Identifier.newInstance(11L))
                .status(AppUpgradeStatus.SUCCESS).build());
        repository.save(AppUpgradeHistory.builder()
                .deploymentId(DEPLOYMENT_ID2)
                .timestamp(Date.from(now.minusSeconds(200)))
                .previousApplicationId(Identifier.newInstance(12L))
                .targetApplicationId(Identifier.newInstance(13L))
                .status(AppUpgradeStatus.SUCCESS).build());

        assertThat(repository.findInPeriod(Date.from(now.minusSeconds(100)), Date.from(now.plusSeconds(100))).size()).isEqualTo(1L);
        assertThat(repository.findInPeriod(Date.from(now.minusSeconds(100)), Date.from(now.plusSeconds(100))).get(0).getDeploymentId())
                .isEqualTo(DEPLOYMENT_ID1);
        assertThat(repository.findInPeriod(Date.from(now.minusSeconds(300)), Date.from(now.minusSeconds(50))).get(0).getDeploymentId())
                .isEqualTo(DEPLOYMENT_ID2);
    }

}
