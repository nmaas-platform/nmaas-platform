package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.model.NamespaceConfigOption;
import net.geant.nmaas.orchestration.entities.DomainTechDetails;
import net.geant.nmaas.orchestration.repositories.DomainTechDetailsRepository;
import net.geant.nmaas.portal.persistent.entity.Domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KubernetesClusterDeploymentManagerTest {

    private static final String DOMAIN = "testDomain";

    private final DomainTechDetailsRepository domainTechDetailsRepository = mock(DomainTechDetailsRepository.class);

    private KubernetesClusterDeploymentManager manager;

    @BeforeEach
    public void setup() {
        manager = new KubernetesClusterDeploymentManager(domainTechDetailsRepository);
    }

    @Test
    public void shouldReturnEmptyStorageClassName() throws UnknownHostException {
        manager.setNamespaceConfigOption(NamespaceConfigOption.USE_DEFAULT_NAMESPACE);
        manager.setDefaultNamespace("testNamespace");
        manager.setDefaultStorageClass(null);
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.empty());
        assertThat(manager.getStorageClass(DOMAIN).isPresent(), is(false));
    }

    @Test
    public void shouldReturnProperStorageClassName() {
        manager.setDefaultStorageClass("testClusterStorageClass");
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.empty());
        assertThat(manager.getStorageClass(DOMAIN).get(), is(manager.getKClusterDeploymentView().getDefaultStorageClass()));

        DomainTechDetails domainTechDetails = new DomainTechDetails(1L, DOMAIN, null, "domainNamespace", null, null);
        Domain domain = new Domain("Domain Name", DOMAIN, false);
        domain.setDomainTechDetails(domainTechDetails);
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.of(domain.getDomainTechDetails()));
        assertThat(manager.getStorageClass(DOMAIN).get(), is(manager.getKClusterDeploymentView().getDefaultStorageClass()));

        domainTechDetails = new DomainTechDetails(1L, DOMAIN, null, "domainNamespace", "", null);
        domain.setDomainTechDetails(domainTechDetails);
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.of(domain.getDomainTechDetails()));
        assertThat(manager.getStorageClass(DOMAIN).get(), is(manager.getKClusterDeploymentView().getDefaultStorageClass()));

        domainTechDetails = new DomainTechDetails(1L, DOMAIN, null, "domainNamespace", "domainStorageClass", null);
        domain.setDomainTechDetails(domainTechDetails);
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.of(domain.getDomainTechDetails()));
        assertThat(manager.getStorageClass(DOMAIN).get(), is(domain.getDomainTechDetails().getKubernetesStorageClass()));
    }

    @Test
    public void shouldThrowExceptionDuringNamespaceConfigOptionValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.setNamespaceConfigOption(NamespaceConfigOption.USE_DEFAULT_NAMESPACE);
            manager.setDefaultNamespace(null);
            manager.getNamespaceConfigOption().validate(manager.getKClusterDeploymentView());
        });
    }

    @Test
    public void shouldReturnProperNamespace() {
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.empty());
        manager.setNamespaceConfigOption(NamespaceConfigOption.USE_DEFAULT_NAMESPACE);
        manager.setDefaultNamespace("testNamespace");
        assertThat(manager.namespace(DOMAIN), is("testNamespace"));
    }

    @Test
    public void shouldReturnProperNamespaceFromDomain() {
        DomainTechDetails domainTechDetails = new DomainTechDetails(1L, DOMAIN, null, "domainNamespace", null, null);
        Domain domain = new Domain("Domain Name", DOMAIN, false);
        domain.setDomainTechDetails(domainTechDetails);
        when(domainTechDetailsRepository.findByDomainCodename(DOMAIN)).thenReturn(Optional.of(domain.getDomainTechDetails()));
        manager.setNamespaceConfigOption(NamespaceConfigOption.USE_DOMAIN_NAMESPACE);
        manager.setDefaultNamespace("testNamespace");
        assertThat(manager.namespace(DOMAIN), is("domainNamespace"));
    }

}
