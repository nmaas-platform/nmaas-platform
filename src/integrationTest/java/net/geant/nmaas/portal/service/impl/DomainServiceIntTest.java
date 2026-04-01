package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.api.dto.domains.DcnDeploymentTypeDto;
import net.geant.nmaas.api.dto.domains.DomainDcnDetailsDto;
import net.geant.nmaas.api.dto.domains.DomainRequest;
import net.geant.nmaas.api.dto.domains.DomainTechDetailsDto;
import net.geant.nmaas.kubernetes.DummyKubernetesApiClientServiceConfig;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.repositories.WebhookEventRepository;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Import(DummyKubernetesApiClientServiceConfig.class)
public class DomainServiceIntTest {

    @MockitoBean
    private WebhookEventRepository webhookEventRepository;

    @Autowired
    private DomainService domainService;

    @Test
    void shouldRemoveDomainAndAllowForCreation() {
        DomainRequest domainRequest1 = new DomainRequest(
                "domainName",
                "domain",
                new DomainDcnDetailsDto(null, null, true, DcnDeploymentTypeDto.MANUAL, null),
                new DomainTechDetailsDto(null, null, "external@domain", null, null, null),
                true,
                new ArrayList<>());
        DomainRequest domainRequest2 = new DomainRequest(
                "domainName",
                "domain",
                new DomainDcnDetailsDto(null, null, true, DcnDeploymentTypeDto.MANUAL, null),
                new DomainTechDetailsDto(null, null, "external@domain", null, null, null),
                true,
                new ArrayList<>());

        domainService.createDomain(domainRequest1);

        assertThat(domainService.getDomains().stream().map(Domain::getName).toList()).contains("GLOBAL", "domainName");

        Long domainId = domainService.findDomain("domainName").orElseThrow().getId();
        domainService.softRemoveDomain(domainId);

        assertThat(domainService.getDomains().size()).isEqualTo(1);
        assertThat(domainService.findDomain(domainId).isEmpty());

        assertDoesNotThrow(() ->
                domainService.createDomain(domainRequest2)
        );
        assertThat(domainService.getDomains().size()).isEqualTo(2);
    }

}
