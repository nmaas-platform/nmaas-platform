package net.geant.nmaas.externalservices.kubernetes;

import net.geant.nmaas.externalservices.kubernetes.model.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.kubernetes.model.IngressResourceConfigOption;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import net.geant.nmaas.portal.persistent.entity.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KubernetesClusterIngressManagerTest {

    private static final String DOMAIN = "testDomain";

    private final DomainTechDetailsRepository domainTechDetailsRepository = mock(DomainTechDetailsRepository.class);

    private KubernetesClusterIngressManager manager;

    @BeforeEach
    public void setup() {
        manager = new KubernetesClusterIngressManager(domainTechDetailsRepository);
    }

    @Test
    public void shouldReturnProperExternalServiceDomain() {
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
    public void shouldThrowExceptionOnMissingDomain() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setIngressPerDomain(true);
            manager.setExternalServiceDomain("testClusterServiceDomain");
            when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.empty());
            manager.getExternalServiceDomain(DOMAIN);
        });
    }

    @Test
    public void shouldProceedWithUseExistingControllerConfigOption() {
        assertDoesNotThrow(() -> {
            manager.setControllerConfigOption(IngressControllerConfigOption.USE_EXISTING);
            manager.setSupportedIngressClass("class");
            manager.setControllerChartName(null);
            manager.setControllerChartArchive(null);
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    public void shouldThrowExceptionDuringIngressControllerConfigValidationExisting() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_REPO);
            manager.setSupportedIngressClass(null);
            manager.setControllerChartName(null);
            manager.setControllerChartArchive(null);
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    public void shouldThrowExceptionDuringIngressControllerConfigValidationRepo() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_REPO);
            manager.setControllerChartName(null);
            manager.setControllerChartArchive("chart");
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    public void shouldThrowExceptionDuringIngressControllerConfigValidationArchive() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_ARCHIVE);
            manager.setControllerChartArchive(null);
            manager.setControllerChartName("chart");
            manager.getControllerConfigOption().validate(manager.getKClusterIngressView());
        });
    }

    @Test
    public void shouldThrowExceptionDuringIngressResourceConfigValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setResourceConfigOption(IngressResourceConfigOption.DEPLOY_FROM_CHART);
            manager.setExternalServiceDomain(null);
            manager.getResourceConfigOption().validate(manager.getKClusterIngressView());
        });
    }

}
