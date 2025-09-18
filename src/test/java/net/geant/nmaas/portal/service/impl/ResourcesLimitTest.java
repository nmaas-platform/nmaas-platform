package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.kubernetes.KubernetesApiClientService;
import net.geant.nmaas.kubernetes.KubernetesClusterDeploymentManager;
import net.geant.nmaas.kubernetes.remote.repositories.KClusterRepository;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.portal.api.domain.DomainBase;
import net.geant.nmaas.portal.api.domain.RejectionReason;
import net.geant.nmaas.portal.api.domain.ResourcesLimitDto;
import net.geant.nmaas.portal.api.domain.ResourcesLimitUpdateDto;
import net.geant.nmaas.portal.api.domain.ResourcesLimitValidationResult;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistent.entity.ResourcesLimitType;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.ResourcesLimitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

        ResourcesLimit domainLimit = Mockito.mock(ResourcesLimit.class);
        when(domainLimit.getInstancesNo()).thenReturn(5);
        when(domainLimit.getContainersNo()).thenReturn(10);
        when(resourcesLimitRepository.findByDomain_Codename(domainCodename)).thenReturn(domainLimit);
        when(resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename)).thenReturn(Collections.emptyList());

        when(appInstanceRepository.countAllActiveInDomain(domainCodename)).thenReturn(2);

        Domain domain = Mockito.mock(Domain.class);
        when(domain.getId()).thenReturn(1L);
        when(domainRepository.findByCodename(domainCodename)).thenReturn(Optional.of(domain));
        when(kClusterRepository.findByDomains_Id(1L)).thenReturn(List.of());

        when(clusterDeploymentManager.namespace(domainCodename)).thenReturn("nmaas-test-domain");
        when(kubernetesApiClientService.getPods(any(), eq("nmaas-test-domain"))).thenReturn(null);

        ResourcesLimitValidationResult result = resourcesLimitService.validateNewDeployment(domainCodename, applicationId, 1, 1);
        assertTrue(result.isAccepted());
    }

    @Test
    void validateNewDeploymentFailsForLimitExceeded() {
        Identifier applicationId = Mockito.mock(Identifier.class);

        ResourcesLimit domainLimit = Mockito.mock(ResourcesLimit.class);
        when(domainLimit.getInstancesNo()).thenReturn(3);
        when(domainLimit.getContainersNo()).thenReturn(10);
        when(resourcesLimitRepository.findByDomain_Codename(domainCodename)).thenReturn(domainLimit);
        when(resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename)).thenReturn(Collections.emptyList());

        when(appInstanceRepository.countAllActiveInDomain(domainCodename)).thenReturn(3);

        ResourcesLimitValidationResult result = resourcesLimitService.validateNewDeployment(domainCodename, applicationId, 1, 1);
        assertFalse(result.isAccepted());
        assertEquals(RejectionReason.DOMAIN_INSTANCES_LIMIT_REACHED, result.getReason());
    }

    @Test
    void validateNewDeploymentWithDomainGroupLimits() {
        Identifier applicationId = Mockito.mock(Identifier.class);

        ResourcesLimit globalLimit = Mockito.mock(ResourcesLimit.class);
        when(globalLimit.getInstancesNo()).thenReturn(10);
        when(globalLimit.getContainersNo()).thenReturn(20);
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(globalLimit);

        when(resourcesLimitRepository.findByDomain_Codename(domainCodename)).thenReturn(null);

        ResourcesLimit groupLimit1 = Mockito.mock(ResourcesLimit.class);
        when(groupLimit1.getInstancesNo()).thenReturn(2);
        when(groupLimit1.getContainersNo()).thenReturn(5);
        
        ResourcesLimit groupLimit2 = Mockito.mock(ResourcesLimit.class);
        when(groupLimit2.getInstancesNo()).thenReturn(3);
        when(groupLimit2.getContainersNo()).thenReturn(8);
        
        when(resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename)).thenReturn(Arrays.asList(groupLimit1, groupLimit2));

        when(appInstanceRepository.countAllActiveInDomain(domainCodename)).thenReturn(2);

        Domain domain = Mockito.mock(Domain.class);
        when(domain.getId()).thenReturn(1L);
        when(domainRepository.findByCodename(domainCodename)).thenReturn(Optional.of(domain));
        when(kClusterRepository.findByDomains_Id(1L)).thenReturn(List.of());

        when(clusterDeploymentManager.namespace(domainCodename)).thenReturn("nmaas-test-domain");
        when(kubernetesApiClientService.getPods(any(), eq("nmaas-test-domain"))).thenReturn(null);

        ResourcesLimitValidationResult result = resourcesLimitService.validateNewDeployment(domainCodename, applicationId, 1, 1);
        assertTrue(result.isAccepted());
    }

    @Test
    void validateNewDeploymentFailsForDomainGroupLimitExceeded() {
        Identifier applicationId = Mockito.mock(Identifier.class);

        ResourcesLimit globalLimit = Mockito.mock(ResourcesLimit.class);
        when(globalLimit.getInstancesNo()).thenReturn(10);
        when(globalLimit.getContainersNo()).thenReturn(20);
        when(resourcesLimitRepository.findByLimitType(ResourcesLimitType.GLOBAL)).thenReturn(globalLimit);

        when(resourcesLimitRepository.findByDomain_Codename(domainCodename)).thenReturn(null);

        ResourcesLimit groupLimit1 = Mockito.mock(ResourcesLimit.class);
        when(groupLimit1.getInstancesNo()).thenReturn(1);
        when(groupLimit1.getContainersNo()).thenReturn(1);
        
        ResourcesLimit groupLimit2 = Mockito.mock(ResourcesLimit.class);
        when(groupLimit2.getInstancesNo()).thenReturn(1);
        when(groupLimit2.getContainersNo()).thenReturn(1);
        
        when(resourcesLimitRepository.findForGroupsBasedOnDomain(domainCodename)).thenReturn(Arrays.asList(groupLimit1, groupLimit2));
        when(appInstanceRepository.countAllActiveInDomain(domainCodename)).thenReturn(12);

        ResourcesLimitValidationResult result = resourcesLimitService.validateNewDeployment(domainCodename, applicationId, 1, 1);
        assertFalse(result.isAccepted());
        assertEquals(RejectionReason.GLOBAL_INSTANCES_LIMIT_REACHED, result.getReason());
    }

}
