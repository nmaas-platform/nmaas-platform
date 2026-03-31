package net.geant.nmaas.nmservice.deployment.limits;

import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Application;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.ResourcesLimitRepository;
import net.geant.nmaas.utils.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ResourcesLimitValidationTest {

    private static final String DOMAIN_CODENAME = "test-domain";

    @MockitoBean
    private ResourcesLimitRepository resourcesLimitRepository;

    @MockitoBean
    private AppInstanceRepository appInstanceRepository;

    @MockitoBean
    private KClusterRepository kClusterRepository;

    @MockitoBean
    private KubernetesApiClientService kubernetesApiClientService;

    @MockitoBean
    private KubernetesClusterDeploymentManager clusterDeploymentManager;

    @Autowired
    private ResourcesLimitValidationService resourcesLimitValidationService;

    @BeforeEach
    void setUp() {
        ResourcesLimit resourcesLimit = new ResourcesLimit(1L, 500, 100, 10, 50, new Domain(1L));
        when(resourcesLimitRepository.save(isA(ResourcesLimit.class))).thenReturn(resourcesLimit);
    }

    @Test
    void validateNewDeploymentPasses() {
        Identifier applicationId = Mockito.mock(Identifier.class);

        ResourcesLimit domainLimit = ResourcesLimit.builder().instancesNo(5).containersNo(10).memory(4028).cpu(1000).build();
        when(resourcesLimitRepository.findByDomain_Codename(DOMAIN_CODENAME)).thenReturn(Optional.of(domainLimit));
        when(resourcesLimitRepository.findForGroupsBasedOnDomain(DOMAIN_CODENAME)).thenReturn(Collections.emptyList());

        List<AppInstance> runningDeployments = List.of(
                createAppInstance(200, 1024),
                createAppInstance(100, 512)
        );

        when(appInstanceRepository.findAllActiveInDomain(DOMAIN_CODENAME))
                .thenReturn(runningDeployments);

        when(kClusterRepository.findByDomains_Id(1L)).thenReturn(List.of());
        when(clusterDeploymentManager.namespace(DOMAIN_CODENAME)).thenReturn("nmaas-test-domain");
        when(kubernetesApiClientService.getPods(any(), eq("nmaas-test-domain"))).thenReturn(null);

        ValidationResult result = resourcesLimitValidationService.validateNewDeployment(DOMAIN_CODENAME, 1, new AppDeploymentSpec());
        assertTrue(result.isAccepted());
    }

    @Test
    void validateNewDeploymentFailsForLimitExceeded() {
        ResourcesLimit domainLimit = ResourcesLimit.builder().instancesNo(2).containersNo(10).memory(4028).cpu(300).build();
        when(resourcesLimitRepository.findByDomain_Codename(DOMAIN_CODENAME)).thenReturn(Optional.of(domainLimit));
        when(resourcesLimitRepository.findForGroupsBasedOnDomain(DOMAIN_CODENAME)).thenReturn(Collections.emptyList());

        List<AppInstance> runningDeployments = List.of(
                createAppInstance(200, 1024),
                createAppInstance(100, 512)
        );

        when(appInstanceRepository.findAllActiveInDomain(DOMAIN_CODENAME)).thenReturn(runningDeployments);

        ValidationResult result = resourcesLimitValidationService.validateNewDeployment(DOMAIN_CODENAME, 1, new AppDeploymentSpec());
        assertFalse(result.isAccepted());
        assertEquals(Stream.of(RejectionReason.DOMAIN_INSTANCES_LIMIT_REACHED.getDescription(), RejectionReason.DOMAIN_CPU_LIMIT_REACHED.getDescription()).collect(Collectors.joining(",")), result.getReasons().stream().map(RejectionReason::getDescription).collect(Collectors.joining(",")));
    }

    @Test
    void validateNewDeploymentWithDomainGroupLimits() {
        ResourcesLimit globalLimit = ResourcesLimit.builder()
                .instancesNo(10).containersNo(20).memory(1024).cpu(500)
                .build();
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(Stream.of(globalLimit).toList());
        when(resourcesLimitRepository.findByDomain_Codename(DOMAIN_CODENAME)).thenReturn(Optional.empty());

        ResourcesLimit groupLimit1 = ResourcesLimit.builder()
                .instancesNo(2).containersNo(5).memory(Utils.DEFAULT_CONSUMED_MEMORY).cpu(Utils.DEFAULT_CONSUMED_CPU)
                .build();
        ResourcesLimit groupLimit2 = ResourcesLimit.builder()
                .instancesNo(2).containersNo(5).memory(Utils.DEFAULT_CONSUMED_MEMORY).cpu(Utils.DEFAULT_CONSUMED_CPU)
                .build();

        when(resourcesLimitRepository.findForGroupsBasedOnDomain(DOMAIN_CODENAME)).thenReturn(Arrays.asList(groupLimit1, groupLimit2));

        List<AppInstance> runningDeployments = List.of(
                createAppInstance(200, 124),
                createAppInstance(100, 112)
        );

        when(appInstanceRepository.findAllActiveInDomain(DOMAIN_CODENAME)).thenReturn(runningDeployments);
        when(kClusterRepository.findByDomains_Id(1L)).thenReturn(List.of());

        when(clusterDeploymentManager.namespace(DOMAIN_CODENAME)).thenReturn("nmaas-test-domain");
        when(kubernetesApiClientService.getPods(any(), eq("nmaas-test-domain"))).thenReturn(null);

        ValidationResult result = resourcesLimitValidationService.validateNewDeployment(DOMAIN_CODENAME, 1, new AppDeploymentSpec());
        assertTrue(result.isAccepted());
    }

    @Test
    void validateNewDeploymentFailsForDomainGroupLimitExceeded() {
        ResourcesLimit globalLimit = ResourcesLimit.builder().instancesNo(1).containersNo(5).memory(128).cpu(300).limitType(ResourcesLimitType.GLOBAL).build();
        when(resourcesLimitRepository.findOneByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(Optional.of(globalLimit));
        when(resourcesLimitRepository.findByDomain_Codename(DOMAIN_CODENAME)).thenReturn(Optional.empty());

        ResourcesLimit groupLimit1 = ResourcesLimit.builder().instancesNo(1).containersNo(1).memory(Utils.DEFAULT_CONSUMED_MEMORY).cpu(Utils.DEFAULT_CONSUMED_CPU).build();
        ResourcesLimit groupLimit2 = ResourcesLimit.builder().instancesNo(1).containersNo(1).memory(Utils.DEFAULT_CONSUMED_MEMORY).cpu(Utils.DEFAULT_CONSUMED_CPU).build();

        when(resourcesLimitRepository.findForGroupsBasedOnDomain(DOMAIN_CODENAME)).thenReturn(Arrays.asList(groupLimit1, groupLimit2));

        List<AppInstance> runningDeployments = List.of(
                createAppInstance(200, 252),
                createAppInstance(100, 252),
                createAppInstance(100, 252)
        );
        when(appInstanceRepository.findAllActiveInDomain(DOMAIN_CODENAME)).thenReturn(runningDeployments);

        ValidationResult result = resourcesLimitValidationService.validateNewDeployment(DOMAIN_CODENAME, 1, new AppDeploymentSpec());
        assertFalse(result.isAccepted());
        assertEquals(Stream.of(RejectionReason.GLOBAL_INSTANCES_LIMIT_REACHED.getDescription(), RejectionReason.GLOBAL_MEMORY_LIMIT_REACHED.getDescription())
                .collect(Collectors.joining(",")), result.getReasons().stream().map(RejectionReason::getDescription).collect(Collectors.joining(",")));
    }

    private AppInstance createAppInstance(int cpu, int memory) {
        AppDeploymentSpec spec = new AppDeploymentSpec();
        spec.setConsumedCpu(cpu);
        spec.setConsumedMemory(memory);

        Application app = new Application();
        app.setAppDeploymentSpec(spec);

        AppInstance instance = new AppInstance();
        instance.setApplication(app);

        return instance;
    }

}
