package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.entities.AppDeploymentSpec;
import net.geant.nmaas.portal.domain.DomainBase;
import net.geant.nmaas.portal.domain.RejectionReason;
import net.geant.nmaas.portal.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.domain.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.domain.ResourcesLimitValidationResult;
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
import org.modelmapper.ModelMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourcesLimitTest {

    private final ResourcesLimitRepository resourcesLimitRepository = mock(ResourcesLimitRepository.class);
    private ResourcesLimitServiceImpl resourcesLimitService ;

    private ResourcesLimitDto resourcesLimitDto;
    private ResourcesLimit resourcesLimit;
    private final AppInstanceRepository appInstanceRepository = mock(AppInstanceRepository.class);
    private KubernetesApiClientService kubernetesApiClientService = mock(KubernetesApiClientService.class);
    private  KubernetesClusterDeploymentManager clusterDeploymentManager = mock(KubernetesClusterDeploymentManager.class);
    private final DomainRepository domainRepository = mock(DomainRepository.class);
    private final KClusterRepository kClusterRepository = mock(KClusterRepository.class);
    private DomainBase domainView = new DomainBase();
    private ModelMapper mapper = new ModelMapper();

    private static final String domainCodename = "test-domain";

    @BeforeEach
    void setUp() {
        resourcesLimitService = new ResourcesLimitServiceImpl(resourcesLimitRepository, appInstanceRepository, kubernetesApiClientService, clusterDeploymentManager, domainRepository, kClusterRepository, mapper);
        domainView.setId(1L);
        resourcesLimitDto = new ResourcesLimitDto(1L, 500, 100, 10, 50, domainView);
        resourcesLimit = new ResourcesLimit(1L, 500, 100, 10, 50, new Domain(1L));
        when(resourcesLimitRepository.save(isA(ResourcesLimit.class))).thenReturn(resourcesLimit);
        resourcesLimitService.create(resourcesLimitDto);
    }

    @Test
    void crudResourcesLimit() {
        DomainBase domainView2 = new DomainBase();
        domainView2.setId(2L);
        ResourcesLimitDto resourcesLimitDto2 = new ResourcesLimitDto(2L, 500, 100, 10, 50, domainView2);
        ResourcesLimit resourcesLimit2 = new ResourcesLimit(2L, 500, 100, 10, 50, new Domain(2L));
        when(resourcesLimitRepository.save(isA(ResourcesLimit.class))).thenReturn(resourcesLimit2);
        ResourcesLimitDto created = resourcesLimitService.create(resourcesLimitDto2);

        assertNotNull(created);
        assertEquals(resourcesLimitDto2.getId(), created.getId());
        assertEquals(100, created.getCpu());
        assertEquals(resourcesLimitDto2.getContainersNo(), created.getContainersNo());
        assertEquals(resourcesLimitDto2.getLimitType(), created.getLimitType());

        ResourcesLimitUpdateDto updateDto = mapper.map(created, ResourcesLimitUpdateDto.class);
        updateDto.setCpu(1000);
        when(resourcesLimitRepository.findById(2L)).thenReturn(Optional.of(resourcesLimit2));
        resourcesLimitService.update(updateDto);
        resourcesLimit2.setCpu(1000);
        when(resourcesLimitRepository.findById(2L)).thenReturn(Optional.of(resourcesLimit2));
        created = resourcesLimitService.getResourcesLimit(2L);
        assertEquals(1000, created.getCpu());
        assertEquals(resourcesLimitDto2.getContainersNo(), created.getContainersNo());
        assertEquals(resourcesLimitDto2.getLimitType(), created.getLimitType());

        doNothing().when(resourcesLimitRepository).deleteById(2L);
        resourcesLimitService.delete(2L);
    }

    @Test
    void shouldGetAllResourcesLimits() {
        when(resourcesLimitRepository.findAll()).thenReturn(Arrays.asList(resourcesLimit));

        List<ResourcesLimitDto> resourcesLimits = resourcesLimitService.getAllResourcesLimits();

        assertNotNull(resourcesLimits);
        assertEquals(1, resourcesLimits.size());
        ResourcesLimitDto created = resourcesLimits.get(0);
        assertEquals(resourcesLimitDto.getId(), created.getId());
        assertEquals(resourcesLimitDto.getCpu(), created.getCpu());
        assertEquals(resourcesLimitDto.getContainersNo(), created.getContainersNo());
        assertEquals(resourcesLimitDto.getLimitType(), created.getLimitType());
    }

    @Test
    void shouldThrowExceptionWhenResourcesLimitNotFound() {
        when(resourcesLimitRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            resourcesLimitService.getResourcesLimit(999L);
        });
    }

    @Test
    void validateNewDeploymentPasses() {
        Identifier applicationId = Mockito.mock(Identifier.class);

        ResourcesLimit domainLimit = ResourcesLimit.builder().instancesNo(5).containersNo(10).memory(4028).cpu(1000).build();
        when(resourcesLimitRepository.findByDomain_Codename(domainCodename)).thenReturn(domainLimit);
        when(resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename)).thenReturn(Collections.emptyList());

        List<AppInstance> runningDeployments = List.of(
                createAppInstance(200, 1024),
                createAppInstance(100, 512)
        );

        when(appInstanceRepository.findAllActiveInDomain(domainCodename)).thenReturn(runningDeployments);

        Domain domain = Mockito.mock(Domain.class);
        when(domain.getId()).thenReturn(1L);
        when(domainRepository.findByCodename(domainCodename)).thenReturn(Optional.of(domain));
        when(kClusterRepository.findByDomains_Id(1L)).thenReturn(List.of());

        when(clusterDeploymentManager.namespace(domainCodename)).thenReturn("nmaas-test-domain");
        when(kubernetesApiClientService.getPods(any(), eq("nmaas-test-domain"))).thenReturn(null);

        ResourcesLimitValidationResult result = resourcesLimitService.validateNewDeployment(domainCodename, applicationId, 1, new AppDeploymentSpec());
        assertTrue(result.isAccepted());
    }

    @Test
    void validateNewDeploymentFailsForLimitExceeded() {
        Identifier applicationId = Mockito.mock(Identifier.class);

        ResourcesLimit domainLimit = ResourcesLimit.builder().instancesNo(2).containersNo(10).memory(4028).cpu(300).build();
        when(resourcesLimitRepository.findByDomain_Codename(domainCodename)).thenReturn(domainLimit);
        when(resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename)).thenReturn(Collections.emptyList());

        List<AppInstance> runningDeployments = List.of(
                createAppInstance(200, 1024),
                createAppInstance(100, 512)
        );

        when(appInstanceRepository.findAllActiveInDomain(domainCodename)).thenReturn(runningDeployments);

        ResourcesLimitValidationResult result = resourcesLimitService.validateNewDeployment(domainCodename, applicationId, 1, new AppDeploymentSpec());
        assertFalse(result.isAccepted());
        assertEquals(Stream.of(RejectionReason.DOMAIN_INSTANCES_LIMIT_REACHED.getDescription(), RejectionReason.DOMAIN_CPU_LIMIT_REACHED.getDescription()).collect(Collectors.joining(",")), result.getReasons().stream().map(RejectionReason::getDescription).collect(Collectors.joining(",")));
    }

    @Test
    void validateNewDeploymentWithDomainGroupLimits() {
        Identifier applicationId = Mockito.mock(Identifier.class);

        ResourcesLimit globalLimit = ResourcesLimit.builder().instancesNo(10).containersNo(20).memory(1024).cpu(500).build();
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(Stream.of(globalLimit).toList());

        when(resourcesLimitRepository.findByDomain_Codename(domainCodename)).thenReturn(null);

        ResourcesLimit groupLimit1 = ResourcesLimit.builder().instancesNo(2).containersNo(5).memory(Utils.DEFAULT_CONSUMED_MEMORY).cpu(Utils.DEFAULT_CONSUMED_CPU).build();
        ResourcesLimit groupLimit2 = ResourcesLimit.builder().instancesNo(2).containersNo(5).memory(Utils.DEFAULT_CONSUMED_MEMORY).cpu(Utils.DEFAULT_CONSUMED_CPU).build();

        when(resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename)).thenReturn(Arrays.asList(groupLimit1, groupLimit2));


        List<AppInstance> runningDeployments = List.of(
                createAppInstance(200, 124),
                createAppInstance(100, 112)
        );

        when(appInstanceRepository.findAllActiveInDomain(domainCodename)).thenReturn(runningDeployments);

        Domain domain = Mockito.mock(Domain.class);
        when(domain.getId()).thenReturn(1L);
        when(domainRepository.findByCodename(domainCodename)).thenReturn(Optional.of(domain));
        when(kClusterRepository.findByDomains_Id(1L)).thenReturn(List.of());

        when(clusterDeploymentManager.namespace(domainCodename)).thenReturn("nmaas-test-domain");
        when(kubernetesApiClientService.getPods(any(), eq("nmaas-test-domain"))).thenReturn(null);

        ResourcesLimitValidationResult result = resourcesLimitService.validateNewDeployment(domainCodename, applicationId, 1, new AppDeploymentSpec());
        assertTrue(result.isAccepted());
    }

    @Test
    void validateNewDeploymentFailsForDomainGroupLimitExceeded() {
        Identifier applicationId = Mockito.mock(Identifier.class);

        ResourcesLimit globalLimit = ResourcesLimit.builder().instancesNo(1).containersNo(5).memory(128).cpu(300).build();
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(Stream.of(globalLimit).toList());

        when(resourcesLimitRepository.findByDomain_Codename(domainCodename)).thenReturn(null);

        ResourcesLimit groupLimit1 = ResourcesLimit.builder().instancesNo(1).containersNo(1).memory(Utils.DEFAULT_CONSUMED_MEMORY).cpu(Utils.DEFAULT_CONSUMED_CPU).build();
        ResourcesLimit groupLimit2 = ResourcesLimit.builder().instancesNo(1).containersNo(1).memory(Utils.DEFAULT_CONSUMED_MEMORY).cpu(Utils.DEFAULT_CONSUMED_CPU).build();
        
        when(resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename)).thenReturn(Arrays.asList(groupLimit1, groupLimit2));
        List<AppInstance> runningDeployments = List.of(
                createAppInstance(200, 252),
                createAppInstance(100, 252),
                createAppInstance(100, 252)
        );

        when(appInstanceRepository.findAllActiveInDomain(domainCodename)).thenReturn(runningDeployments);

        ResourcesLimitValidationResult result = resourcesLimitService.validateNewDeployment(domainCodename, applicationId, 1, new AppDeploymentSpec());
        assertFalse(result.isAccepted());
        assertEquals( Stream.of(RejectionReason.GLOBAL_INSTANCES_LIMIT_REACHED.getDescription(), RejectionReason.GLOBAL_MEMORY_LIMIT_REACHED.getDescription()).collect(Collectors.joining(",")), result.getReasons().stream().map(RejectionReason::getDescription).collect(Collectors.joining(",")));
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
