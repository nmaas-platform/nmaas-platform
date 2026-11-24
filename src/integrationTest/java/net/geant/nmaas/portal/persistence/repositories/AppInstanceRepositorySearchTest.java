package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.AppDeploymentState;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the search filter of the paged instance queries matches
 * both the instance name and the application name, case-insensitively.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@WithMockUser(username = "searchtestowner", roles = {"SYSTEM_ADMIN"})
public class AppInstanceRepositorySearchTest {

    @Autowired
    private AppInstanceRepository appInstanceRepository;

    @Autowired
    private AppDeploymentRepository appDeploymentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private UserRepository userRepository;

    private Application prometheus;
    private Application grafana;
    private Domain domain;
    private User owner;

    @BeforeEach
    void setup() {
        cleanupTestData();

        prometheus = applicationRepository.save(new Application("Prometheus", "1.0.0"));
        grafana = applicationRepository.save(new Application("Grafana", "1.0.0"));
        domain = domainRepository.save(new Domain("searchtest", "searchtest"));
        owner = new User("searchtestowner", true, "password", domain, net.geant.nmaas.portal.persistence.entity.Role.ROLE_USER);
        owner.setEmail("searchtestowner@example.org");
        owner = userRepository.save(owner);

        // instance name matches, application name does not
        saveInstance(prometheus, domain, owner, "my-monitoring", AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        // application name matches, instance name does not
        saveInstance(prometheus, domain, owner, "custom-name", AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        // neither matches
        saveInstance(grafana, domain, owner, "dashboards", AppDeploymentState.APPLICATION_DEPLOYMENT_VERIFIED);
        // matches by application name (grafana) but is removed
        saveInstance(grafana, domain, owner, "grafana-old", AppDeploymentState.APPLICATION_REMOVED);
    }

    @AfterEach
    void cleanup() {
        cleanupTestData();
    }

    /**
     * Removes test data while preserving the seeded admin user and GLOBAL domain
     * that other tests sharing this context (and the AuditorProvider) depend on.
     */
    private void cleanupTestData() {
        appDeploymentRepository.deleteAll();
        appInstanceRepository.deleteAll();
        applicationRepository.deleteAll();
        userRepository.findAll().stream()
                .filter(user -> !user.getUsername().equalsIgnoreCase("admin"))
                .forEach(userRepository::delete);
        domainRepository.findAll().stream()
                .filter(d -> !d.getCodename().equalsIgnoreCase("GLOBAL"))
                .forEach(domainRepository::delete);
    }

    @Test
    void searchShouldMatchInstanceName() {
        List<AppInstance> result = appInstanceRepository.findAllNotDeletedByOwnerAndSearch(
                owner, "my-monitor", PageRequest.of(0, 10), null).getContent();
        assertThat(result).extracting(AppInstance::getName).containsExactly("my-monitoring");
    }

    @Test
    void searchShouldMatchApplicationName() {
        List<AppInstance> result = appInstanceRepository.findAllNotDeletedByOwnerAndSearch(
                owner, "prometheus", PageRequest.of(0, 10), null).getContent();
        assertThat(result).extracting(AppInstance::getName)
                .containsExactlyInAnyOrder("my-monitoring", "custom-name");
    }

    @Test
    void searchShouldBeCaseInsensitiveOnInstanceName() {
        List<AppInstance> result = appInstanceRepository.findAllNotDeletedByOwnerAndSearch(
                owner, "MY-MONITOR", PageRequest.of(0, 10), null).getContent();
        assertThat(result).extracting(AppInstance::getName).containsExactly("my-monitoring");
    }

    @Test
    void searchShouldBeCaseInsensitiveOnApplicationName() {
        List<AppInstance> result = appInstanceRepository.findAllNotDeletedByOwnerAndSearch(
                owner, "PROMETHEUS", PageRequest.of(0, 10), null).getContent();
        assertThat(result).extracting(AppInstance::getName)
                .containsExactlyInAnyOrder("my-monitoring", "custom-name");
    }

    @Test
    void searchShouldBeCaseInsensitivePartialOnApplicationName() {
        List<AppInstance> result = appInstanceRepository.findAllNotDeletedByOwnerAndSearch(
                owner, "rome", PageRequest.of(0, 10), null).getContent();
        assertThat(result).extracting(AppInstance::getName)
                .containsExactlyInAnyOrder("my-monitoring", "custom-name");
    }

    @Test
    void searchWithNullValueShouldReturnAllInstancesOfOwner() {
        List<AppInstance> result = appInstanceRepository.findAllNotDeletedByOwnerAndSearch(
                owner, null, PageRequest.of(0, 10), null).getContent();
        assertThat(result).hasSize(4);
    }

    @Test
    void searchWithDeployFilterShouldExcludeRemovedInstances() {
        Page<AppInstance> result = appInstanceRepository.findAllNotDeletedByOwnerAndByDeployAndSearch(
                owner, "prometheus", true, PageRequest.of(0, 10), null);
        assertThat(result.getContent()).extracting(AppInstance::getName)
                .containsExactlyInAnyOrder("my-monitoring", "custom-name");
    }

    @Test
    void searchWithDeployFilterUndeployedShouldOnlyReturnRemovedInstances() {
        Page<AppInstance> result = appInstanceRepository.findAllNotDeletedByOwnerAndByDeployAndSearch(
                owner, "grafana", false, PageRequest.of(0, 10), null);
        assertThat(result.getContent()).extracting(AppInstance::getName)
                .containsExactly("grafana-old");
    }

    @Test
    void searchShouldCombineWithDomainFilter() {
        List<AppInstance> result = appInstanceRepository.findAllNotDeletedByDomainAndSearch(
                domain, "grafana", PageRequest.of(0, 10), null).getContent();
        assertThat(result).extracting(AppInstance::getName)
                .containsExactlyInAnyOrder("dashboards", "grafana-old");
    }

    @Test
    void searchShouldCombineWithRemoteClusterFilter() {
        List<AppInstance> result = appInstanceRepository.findAllNotDeletedByOwnerAndSearch(
                owner, "prometheus", PageRequest.of(0, 10), 42L).getContent();
        assertThat(result).isEmpty();

        List<AppInstance> all = appInstanceRepository.findAllNotDeletedByOwnerAndSearch(
                owner, null, PageRequest.of(0, 10), null).getContent();
        assertThat(all).hasSize(4);
    }

    private void saveInstance(Application application, Domain domain, User owner, String name, AppDeploymentState state) {
        AppInstance instance = new AppInstance(application, name, domain, owner, false);
        instance.setInternalId(Identifier.newInstance(name));
        instance = appInstanceRepository.save(instance);
        appDeploymentRepository.save(AppDeployment.builder()
                .deploymentId(Identifier.newInstance(name))
                .instanceId(instance.getId())
                .applicationId(Identifier.newInstance(name))
                .descriptiveDeploymentId(Identifier.newInstance(name))
                .domain(domain.getCodename())
                .deploymentName(name)
                .state(state)
                .build());
    }

}
