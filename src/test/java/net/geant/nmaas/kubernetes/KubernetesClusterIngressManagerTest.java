package net.geant.nmaas.kubernetes;

import net.geant.nmaas.kubernetes.remote.entities.IngressControllerConfigOption;
import net.geant.nmaas.kubernetes.remote.entities.IngressResourceConfigOption;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import net.geant.nmaas.portal.persistence.entity.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KubernetesClusterIngressManagerTest {

    private static final String DOMAIN = "testDomain";

    private final DomainTechDetailsRepository domainTechDetailsRepository = mock(DomainTechDetailsRepository.class);

    private KubernetesClusterIngressManager manager;

    @BeforeEach
    void setup() {
        manager = new KubernetesClusterIngressManager(domainTechDetailsRepository);
    }

    @Test
    void shouldReturnProperExternalServiceDomain() {
        manager.setIngressPerDomain(false);
        manager.setExternalServiceDomain("testClusterServiceDomain");
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.empty());
        assertThat(manager.getExternalServiceDomain(DOMAIN), is(manager.getKClusterIngressView().getExternalServiceDomain()));

        DomainTechDetails domainTechDetails = new DomainTechDetails(1L, DOMAIN, "domainServiceDomain", "domainNamespace", "domainStorageClass", null);
        Domain domain = new Domain("Domain Name", DOMAIN, false);
        domain.setDomainTechDetails(domainTechDetails);
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.of(domain.getDomainTechDetails()));
        manager.setIngressPerDomain(true);
        assertThat(manager.getExternalServiceDomain(DOMAIN), is(domain.getDomainTechDetails().getExternalServiceDomain()));
    }

    @Test
    void shouldThrowExceptionOnMissingDomain() {
        manager.setIngressPerDomain(true);
        manager.setExternalServiceDomain("testClusterServiceDomain");
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> {
            manager.getExternalServiceDomain(DOMAIN);
        });
    }

    @Test
    void shouldProceedWithUseExistingControllerConfigOption() {
        manager.setControllerConfigOption(IngressControllerConfigOption.USE_EXISTING);
        manager.setSupportedIngressClass("class");
        manager.setControllerChartName(null);
        manager.setControllerChartArchive(null);
        assertDoesNotThrow(() -> {
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    void shouldThrowExceptionDuringIngressControllerConfigValidationExisting() {
        manager.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_REPO);
        manager.setSupportedIngressClass(null);
        manager.setControllerChartName(null);
        manager.setControllerChartArchive(null);
        assertThrows(IllegalArgumentException.class, () -> {
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    void shouldThrowExceptionDuringIngressControllerConfigValidationRepo() {
        manager.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_REPO);
        manager.setControllerChartName(null);
        manager.setControllerChartArchive("chart");
        assertThrows(IllegalArgumentException.class, () -> {
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    void shouldThrowExceptionDuringIngressControllerConfigValidationArchive() {
        manager.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_ARCHIVE);
        manager.setControllerChartArchive(null);
        manager.setControllerChartName("chart");
        assertThrows(IllegalArgumentException.class, () -> {
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    void shouldThrowExceptionDuringIngressResourceConfigValidation() {
        manager.setResourceConfigOption(IngressResourceConfigOption.DEPLOY_FROM_CHART);
        manager.setExternalServiceDomain(null);
        assertThrows(IllegalArgumentException.class, () -> {
            manager.getResourceConfigOption().validate(manager.getKClusterIngressView());
        });
    }

}
