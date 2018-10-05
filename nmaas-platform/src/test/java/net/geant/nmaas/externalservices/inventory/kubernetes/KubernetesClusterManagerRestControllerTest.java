package net.geant.nmaas.externalservices.inventory.kubernetes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.inventory.kubernetes.model.KubernetesClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressControllerConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.IngressResourceConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterApi;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterAttachPoint;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterDeployment;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterExtNetwork;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterHelm;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KClusterIngress;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.NamespaceConfigOption;
import net.geant.nmaas.externalservices.inventory.kubernetes.exceptions.KubernetesClusterNotFoundException;
import net.geant.nmaas.externalservices.inventory.kubernetes.repositories.KubernetesClusterRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-k8s.properties")
public class KubernetesClusterManagerRestControllerTest {

    private final static String URL_PREFIX = "/api/management/kubernetes";

    private final static String KUBERNETES_CLUSTER_JSON =
            "{" +
                    "\"name\":\"K8S-NAME-1\"," +
                    "\"helm\":{" +
                        "\"helmHostAddress\":\"192.168.0.1\"," +
                        "\"helmHostSshUsername\":\"testuser\"," +
                        "\"useLocalChartArchives\":\"true\"," +
                        "\"helmHostChartsDirectory\":\"/home/testuser/charts\"" +
                    "}," +
                    "\"api\":{" +
                        "\"restApiHostAddress\":\"192.168.0.8\"," +
                        "\"restApiPort\":9999" +
                    "}," +
                    "\"ingress\": {" +
                        "\"controllerConfigOption\":\"USE_EXISTING\"," +
                        "\"supportedIngressClass\":\"ingress-class\"," +
                        "\"controllerChartArchive\":\"chart.tgz\"," +
                        "\"resourceConfigOption\":\"DEPLOY_FROM_CHART\"," +
                        "\"externalServiceDomain\":\"test.net\"," +
                        "\"tlsSupported\":false" +
                    "}," +
                    "\"deployment\": {" +
                        "\"namespaceConfigOption\":\"USE_DEFAULT_NAMESPACE\"," +
                        "\"defaultNamespace\":\"testNamespace\"," +
                        "\"defaultStorageClass\":\"storageClass\"," +
                        "\"useInClusterGitLabInstance\":\"false\"," +
                        "\"smtpServerAddress\": \"nmaas-postfix\"," +
                        "\"smtpServerPort\": 587" +
                    "}," +
                    "\"attachPoint\":{" +
                        "\"routerName\":\"R1\"," +
                        "\"routerId\":\"172.0.0.1\"," +
                        "\"routerInterfaceName\":\"ge-0/0/1\"" +
                    "}," +
                    "\"externalNetworks\":" +
                        "[{" +
                        "\"externalIp\":\"10.0.0.1\"," +
                        "\"externalNetwork\":\"10.0.0.0\"," +
                        "\"externalNetworkMaskLength\":24," +
                        "\"assigned\":false" +
                        "}," +
                        "{" +
                        "\"externalIp\":\"10.0.1.1\"," +
                        "\"externalNetwork\":\"10.0.1.0\"," +
                        "\"externalNetworkMaskLength\":24," +
                        "\"assigned\":false" +
                        "}" +
                    "]" +
                "}";

    @Autowired
    private KubernetesClusterManager clusterManager;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private KubernetesClusterRepository clusterRepository;

    private MockMvc mvc;

    @Before
    public void init() {
        mvc = MockMvcBuilders.standaloneSetup(new KubernetesClusterManagerRestController(clusterManager)).build();
    }

    @After
    public void clean() throws KubernetesClusterNotFoundException {
        clusterRepository.deleteAll();
    }

    @Test
    public void shouldAddAndRemoveNewKubernetesCluster() throws Exception {
        long sizeBefore = clusterManager.getAllClusters().size();
        MvcResult result = mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();

        Long newId = Long.parseLong(result.getResponse().getContentAsString());

        assertEquals(sizeBefore + 1, clusterManager.getAllClusters().size());
        mvc.perform(delete(URL_PREFIX + "/{id}", newId))
                .andExpect(status().isNoContent());
        assertEquals(sizeBefore, clusterManager.getAllClusters().size());
    }

    @Test
    public void shouldAddNewKubernetesClusterFromJson() throws Exception {
        long sizeBefore = clusterManager.getAllClusters().size();
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(KUBERNETES_CLUSTER_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertEquals(sizeBefore + 1, clusterManager.getAllClusters().size());
    }

    @Test
    public void shouldNotAddExistingKubernetesCluster() throws Exception {
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldNotRemoveNotExistingKubernetesCluster() throws Exception {
        mvc.perform(delete(URL_PREFIX + "/{id}", -1))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void shouldUpdateKubernetesCluster() throws Exception {
        MvcResult createResult = mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();

        Long clusterId = Long.parseLong(createResult.getResponse().getContentAsString());

        KCluster updated = initNewKubernetesCluster();
        updated.getApi().setRestApiPort(350);
        mvc.perform(put(URL_PREFIX + "/{id}", clusterId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updated))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        MvcResult result = mvc.perform(get(URL_PREFIX + "/{id}", clusterId))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("350"));
    }

    @Test
    public void shouldNotUpdateNotExistingKubernetesCluster() throws Exception {
        mvc.perform(put(URL_PREFIX + "/{id}", -1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldListAllKubernetesClusters() throws Exception {
        MvcResult result = mvc.perform(get(URL_PREFIX))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                clusterManager.getAllClusters().size(),
                ((List<KCluster>) new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<List<KCluster>>() {})).size());
    }

    @Test
    public void shouldFetchKubernetesClusterById() throws Exception {
        MvcResult createResult = mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()).andReturn();

        Long newId = Long.parseLong(createResult.getResponse().getContentAsString());

        MvcResult result = mvc.perform(get(URL_PREFIX + "/{id}", newId))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                "testNamespace",
                ((KCluster) new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<KCluster>() {})).getDeployment().getDefaultNamespace());
    }

    @Test
    public void shouldNotFetchNotExistingKubernetesClusterById() throws Exception {
        mvc.perform(get(URL_PREFIX + "/{id}", -1))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldMapKubernetesClusterToKubernetesClusterView() throws UnknownHostException {
        KCluster source = initNewKubernetesCluster();
        KubernetesClusterView output = modelMapper.map(source, KubernetesClusterView.class);
        assertThat(output.getHelmHostAddress().getHostAddress(), equalTo(source.getHelm().getHelmHostAddress().getHostAddress()));
        assertThat(output.getRestApiHostAddress().getHostAddress(), equalTo(source.getApi().getRestApiHostAddress().getHostAddress()));
        assertThat(output.getRestApiPort(), equalTo(source.getApi().getRestApiPort()));
    }

    private KCluster initNewKubernetesCluster() throws UnknownHostException {
        KCluster cluster = new KCluster();
        KClusterHelm helm = new KClusterHelm();
        helm.setHelmHostAddress(InetAddress.getByName("192.168.0.1"));
        helm.setHelmHostSshUsername("testuser");
        helm.setUseLocalChartArchives(true);
        helm.setHelmHostChartsDirectory("/home/testuser/charts");
        cluster.setHelm(helm);
        KClusterApi api = new KClusterApi();
        api.setRestApiHostAddress(InetAddress.getByName("192.168.0.8"));
        api.setRestApiPort(9999);
        cluster.setApi(api);
        KClusterIngress ingress = new KClusterIngress();
        ingress.setControllerConfigOption(IngressControllerConfigOption.USE_EXISTING);
        ingress.setSupportedIngressClass("ingress-class");
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
        deployment.setSmtpServerAddress("test-postfix");
        deployment.setSmtpServerPort(543);
        cluster.setDeployment(deployment);
        KClusterAttachPoint attachPoint = new KClusterAttachPoint();
        attachPoint.setRouterId("172.0.0.1");
        attachPoint.setRouterInterfaceName("ge-0/0/1");
        attachPoint.setRouterName("R1");
        cluster.setAttachPoint(attachPoint);
        KClusterExtNetwork externalNetwork1 = new KClusterExtNetwork();
        externalNetwork1.setExternalIp(InetAddress.getByName("10.0.0.1"));
        externalNetwork1.setExternalNetwork(InetAddress.getByName("10.0.0.0"));
        externalNetwork1.setExternalNetworkMaskLength(24);
        KClusterExtNetwork externalNetwork2 = new KClusterExtNetwork();
        externalNetwork2.setExternalIp(InetAddress.getByName("10.0.1.1"));
        externalNetwork2.setExternalNetwork(InetAddress.getByName("10.0.1.0"));
        externalNetwork2.setExternalNetworkMaskLength(24);
        cluster.setExternalNetworks(Arrays.asList(externalNetwork1, externalNetwork2));
        return cluster;
    }

}
