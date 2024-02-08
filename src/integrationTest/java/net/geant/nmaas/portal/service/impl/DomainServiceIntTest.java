package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.portal.api.domain.DomainDcnDetailsView;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.DomainTechDetailsView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DomainServiceIntTest {

    @Autowired
    private DomainService domainService;

    @Test
    void shouldRemoveDomainAndAllowForCreation() {
        DomainRequest domainRequest1 = new DomainRequest(
                "domainName",
                "domain",
                new DomainDcnDetailsView(null, null, true, DcnDeploymentType.MANUAL, null),
                new DomainTechDetailsView(null, null, "external@domain", null, null, null),
                true,
                new ArrayList<>());
        DomainRequest domainRequest2 = new DomainRequest(
                "domainName",
                "domain",
                new DomainDcnDetailsView(null, null, true, DcnDeploymentType.MANUAL, null),
                new DomainTechDetailsView(null, null, "external@domain", null, null, null),
                true,
                new ArrayList<>());

        domainService.createDomain(domainRequest1);

        assertThat(domainService.getDomains().stream().map(Domain::getName).collect(Collectors.toList())).contains("GLOBAL", "domainName");

        Long domainId = domainService.findDomain("domainName").orElseThrow().getId();
        domainService.softRemoveDomain(domainId);

        assertThat(domainService.getDomains().size()).isEqualTo(1);
        assertThat(domainService.findDomain(domainId).orElseThrow().getName()).matches(val -> val.contains("DELETED"));

        assertDoesNotThrow(() ->
                domainService.createDomain(domainRequest2)
        );
        assertThat(domainService.getDomains().size()).isEqualTo(2);
    }

}
