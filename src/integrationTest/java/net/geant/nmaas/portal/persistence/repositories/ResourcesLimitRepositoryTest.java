package net.geant.nmaas.portal.persistence.repositories;

import jakarta.transaction.Transactional;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResourcesLimitRepositoryTest {

    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private ResourcesLimitRepository resourcesLimitRepository;

    @Test
    @Transactional
    void shouldNotMatchLimitsWithoutDomainWhenCodenameIsNull() {
        Domain domain = domainRepository.save(new Domain("limit-domain", "limit-domain"));
        resourcesLimitRepository.save(ResourcesLimit.builder()
                .memory(100)
                .cpu(100)
                .instancesNo(1)
                .containersNo(1)
                .limitType(ResourcesLimitType.GLOBAL)
                .build());
        resourcesLimitRepository.save(ResourcesLimit.builder()
                .memory(200)
                .cpu(200)
                .instancesNo(2)
                .containersNo(2)
                .limitType(ResourcesLimitType.DOMAIN)
                .domain(domain)
                .build());

        assertThat(resourcesLimitRepository.findByDomain_Codename(null)).isEmpty();
    }

    @Test
    @Transactional
    void shouldFindLimitByDomainCodename() {
        Domain domain = domainRepository.save(new Domain("limit-domain-2", "limit-domain-2"));
        ResourcesLimit domainLimit = resourcesLimitRepository.save(ResourcesLimit.builder()
                .memory(200)
                .cpu(200)
                .instancesNo(2)
                .containersNo(2)
                .limitType(ResourcesLimitType.DOMAIN)
                .domain(domain)
                .build());

        assertThat(resourcesLimitRepository.findByDomain_Codename("limit-domain-2"))
                .contains(domainLimit);
    }
}
