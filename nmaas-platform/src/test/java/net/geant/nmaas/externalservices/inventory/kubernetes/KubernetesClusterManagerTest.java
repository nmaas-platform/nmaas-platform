package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterApi;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterAttachPoint;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterDeployment;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterExtNetwork;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.KClusterExtNetworkView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterIngress;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.NamespaceConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.ExternalNetworkNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.impl.DomainServiceImpl;
import net.geant.nmaas.portal.service.impl.domains.DefaultCodenameValidator;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KubernetesClusterManagerTest {

    private static final String REST_API_HOST_ADDRESS = "10.10.1.1";
    private static final int REST_API_PORT = 9000;
    private static final String HELM_HOST_CHARTS_DIRECTORY = "/home/charts";
    private static final String HELM_HOST_ADDRESS = "10.10.1.2";
    private static final String HELM_HOST_SSH_USERNAME = "test";
    private static final String DOMAIN = "testDomain";

    DomainServiceImpl.CodenameValidator namespaceValidator;
    private KubernetesClusterRepository repository = mock(KubernetesClusterRepository.class);
    private DomainService domainService = mock(DomainService.class);

    private KubernetesClusterManager manager;

    @Before
    public void setup() {
        namespaceValidator = new DefaultCodenameValidator("[a-z-]{0,64}");
        manager = new KubernetesClusterManager(repository, null, namespaceValidator, domainService);
    }

    @Test
    public void shouldReserveExternalNetworks() throws UnknownHostException, ExternalNetworkNotFoundException {
        when(repository.count()).thenReturn(1L);
        when(repository.findAll()).thenReturn(Arrays.asList(simpleKubernetesCluster()));
        String domain10 = "domain10";
        KClusterExtNetworkView network10 = manager.reserveExternalNetwork(domain10);
        String domain20 = "domain20";
        KClusterExtNetworkView network20 = manager.reserveExternalNetwork(domain20);
        assertThat(network10.getExternalIp().getHostAddress(), not(equalTo(network20.getExternalIp().getHostAddress())));
    }

    @Test(expected = ExternalNetworkNotFoundException.class)
    public void shouldFailToReserveExternalNetworks() throws UnknownHostException, ExternalNetworkNotFoundException {
        when(repository.count()).thenReturn(1L);
        when(repository.findAll()).thenReturn(Arrays.asList(simpleKubernetesCluster()));
        manager.reserveExternalNetwork("domain10");
        manager.reserveExternalNetwork("domain20");
        manager.reserveExternalNetwork("domain30");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDuringIngressControllerConfigValidation() throws UnknownHostException {
        KClusterIngress ingress1 = simpleKubernetesCluster().getIngress();
        ingress1.setControllerConfigOption(IngressControllerConfigOption.DEPLOY_NEW_FROM_ARCHIVE);
        ingress1.setControllerChartArchive(null);
        ingress1.getControllerConfigOption().validate(ingress1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionDuringIngressResourceConfigValidation() throws UnknownHostException {
        KClusterIngress ingress1 = simpleKubernetesCluster().getIngress();
        ingress1.setResourceConfigOption(IngressResourceConfigOption.DEPLOY_FROM_CHART);
        ingress1.setExternalServiceDomain(null);
        ingress1.getResourceConfigOption().validate(ingress1);
    }

    @Test
    public void shouldReturnEmptyStorageClassName() throws UnknownHostException {
        when(repository.count()).thenReturn(1L);
        KCluster clusterWithoutStorageClass = simpleKubernetesCluster();
        KClusterDeployment deploymentWithoutStorageClass = new KClusterDeployment();
        deploymentWithoutStorageClass.setNamespaceConfigOption(NamespaceConfigOption.USE_DEFAULT_NAMESPACE);
        deploymentWithoutStorageClass.setDefaultNamespace("testNamespace");
        deploymentWithoutStorageClass.setDefaultStorageClass(null);
        deploymentWithoutStorageClass.setUseInClusterGitLabInstance(false);
        clusterWithoutStorageClass.setDeployment(deploymentWithoutStorageClass);
        when(repository.findAll()).thenReturn(Arrays.asList(clusterWithoutStorageClass));
        when(domainService.findDomainByCodename(DOMAIN)).thenReturn(Optional.empty());
        assertThat(manager.getStorageClass(DOMAIN).isPresent(), is(false));
    }

    @Test
    public void shouldReturnProperStorageClassName() throws UnknownHostException {
        when(repository.count()).thenReturn(1L);
        when(repository.findAll()).thenReturn(Arrays.asList(simpleKubernetesCluster()));
        when(domainService.findDomainByCodename(DOMAIN)).thenReturn(Optional.empty());
        KCluster cluster = simpleKubernetesCluster();
        assertThat(manager.getStorageClass(DOMAIN).get(), is(cluster.getDeployment().getDefaultStorageClass()));

        Domain domain = new Domain("Domain Name", DOMAIN, false, "domainNamespace", null);
        when(domainService.findDomainByCodename(DOMAIN)).thenReturn(Optional.of(domain));
        assertThat(manager.getStorageClass(DOMAIN).get(), is(cluster.getDeployment().getDefaultStorageClass()));

        domain = new Domain("Domain Name", DOMAIN, false, "domainNamespace", "");
        when(domainService.findDomainByCodename(DOMAIN)).thenReturn(Optional.of(domain));
        assertThat(manager.getStorageClass(DOMAIN).get(), is(cluster.getDeployment().getDefaultStorageClass()));

        domain = new Domain("Domain Name", DOMAIN, false, "domainNamespace", "domainStorageClass");
        when(domainService.findDomainByCodename(DOMAIN)).thenReturn(Optional.of(domain));
        assertThat(manager.getStorageClass(DOMAIN).get(), is(domain.getDomainTechDetails().getKubernetesStorageClass()));
    }

    private KCluster simpleKubernetesCluster() throws UnknownHostException {
        KCluster cluster = new KCluster();
        KClusterApi api = new KClusterApi();
        api.setRestApiHostAddress(InetAddress.getByName(REST_API_HOST_ADDRESS));
        api.setRestApiPort(REST_API_PORT);
        cluster.setApi(api);
        KClusterIngress ingress = new KClusterIngress();
        ingress.setControllerConfigOption(IngressControllerConfigOption.USE_EXISTING);
        ingress.setControllerChartArchive("chart.tgz");
        ingress.setResourceConfigOption(IngressResourceConfigOption.DEPLOY_FROM_CHART);
        ingress.setExternalServiceDomain("test.net");
        ingress.setTlsSupported(false);
        cluster.setIngress(ingress);
        KClusterDeployment deployment = new KClusterDeployment();
        deployment.setNamespaceConfigOption(NamespaceConfigOption.USE_DEFAULT_NAMESPACE);
        deployment.setDefaultNamespace("testNamespace");
        deployment.setDefaultStorageClass("storageClass");
        deployment.setUseInClusterGitLabInstance(false);
        deployment.setSmtpServerHostname("test-postfix");
        deployment.setSmtpServerPort(543);
        cluster.setDeployment(deployment);
        KClusterAttachPoint attachPoint = new KClusterAttachPoint();
        attachPoint.setRouterName("R1");
        attachPoint.setRouterId("172.0.0.1");
        attachPoint.setRouterInterfaceName("ge-0/0/1");
        cluster.setAttachPoint(attachPoint);
        KClusterExtNetwork externalNetworkSpec1 = new KClusterExtNetwork();
        externalNetworkSpec1.setExternalIp(InetAddress.getByName("192.168.1.1"));
        externalNetworkSpec1.setExternalNetwork(InetAddress.getByName("192.168.1.0"));
        externalNetworkSpec1.setExternalNetworkMaskLength(24);
        KClusterExtNetwork externalNetworkSpec2 = new KClusterExtNetwork();
        externalNetworkSpec2.setExternalIp(InetAddress.getByName("192.168.2.1"));
        externalNetworkSpec2.setExternalNetwork(InetAddress.getByName("192.168.2.0"));
        externalNetworkSpec2.setExternalNetworkMaskLength(24);
        cluster.setExternalNetworks(Arrays.asList(externalNetworkSpec1, externalNetworkSpec2));
        return cluster;
    }

}
