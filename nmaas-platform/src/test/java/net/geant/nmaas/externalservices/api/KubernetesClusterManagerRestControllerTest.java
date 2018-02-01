package net.geant.nmaas.externalservices.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.externalservices.api.model.KubernetesClusterView;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.ExternalNetworkSpec;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesCluster;
import net.geant.nmaas.externalservices.inventory.kubernetes.entities.KubernetesClusterAttachPoint;
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

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-k8s.properties")
public class KubernetesClusterManagerRestControllerTest {

    private final static String NEW_KUBERNETES_CLUSTER_NAME = "K8S-NAME-1";
    private final static String DIFFERENT_KUBERNETES_CLUSTER_NAME = "DIFFERENT-K8S-NAME-1";
    private final static String URL_PREFIX = "/platform/api/management/kubernetes";

    private final static String KUBERNETES_CLUSTER_JSON =
            "{" +
                    "\"name\":\"K8S-NAME-1\"," +
                    "\"helmHostAddress\":\"192.168.0.1\"," +
                    "\"helmHostSshUsername\":\"testuser\"," +
                    "\"helmHostChartsDirectory\":\"/home/testuser/charts\"," +
                    "\"restApiHostAddress\":\"192.168.0.8\"," +
                    "\"restApiPort\":9999," +
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
    private KubernetesClusterRepository repository;
    @Autowired
    private ModelMapper modelMapper;
    private MockMvc mvc;

    @Before
    public void init() {
        mvc = MockMvcBuilders.standaloneSetup(new KubernetesClusterManagerRestController(repository, modelMapper)).build();
    }

    @After
    public void clean() {
        repository.deleteAll();
    }

    @Test
    public void shouldAddAndRemoveNewKubernetesCluster() throws Exception {
        long sizeBefore = repository.count();
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster(NEW_KUBERNETES_CLUSTER_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertEquals(sizeBefore + 1, repository.count());
        mvc.perform(delete(URL_PREFIX + "/{name}", NEW_KUBERNETES_CLUSTER_NAME))
                .andExpect(status().isNoContent());
        assertEquals(sizeBefore, repository.count());
    }


    @Test
    public void shouldAddNewKubernetesClusterFromJson() throws Exception {
        long sizeBefore = repository.count();
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(KUBERNETES_CLUSTER_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertEquals(sizeBefore + 1, repository.count());
    }

    @Test
    public void shouldNotAddExistingKubernetesCluster() throws Exception {
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster(NEW_KUBERNETES_CLUSTER_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster(NEW_KUBERNETES_CLUSTER_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void shouldNotRemoveNotExistingKubernetesCluster() throws Exception {
        mvc.perform(delete(URL_PREFIX + "/{name}", DIFFERENT_KUBERNETES_CLUSTER_NAME))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    public void shouldUpdateKubernetesCluster() throws Exception {
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster(NEW_KUBERNETES_CLUSTER_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        KubernetesCluster updated = initNewKubernetesCluster(NEW_KUBERNETES_CLUSTER_NAME);
        updated.setRestApiPort(350);
        mvc.perform(put(URL_PREFIX + "/{name}", NEW_KUBERNETES_CLUSTER_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(updated))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        MvcResult result = mvc.perform(get(URL_PREFIX + "/{name}", NEW_KUBERNETES_CLUSTER_NAME))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("350"));
    }

    @Test
    public void shouldNotUpdateNotExistingKubernetesCluster() throws Exception {
        mvc.perform(put(URL_PREFIX + "/{name}", DIFFERENT_KUBERNETES_CLUSTER_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster(DIFFERENT_KUBERNETES_CLUSTER_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldListAllKubernetesClusters() throws Exception {
        MvcResult result = mvc.perform(get(URL_PREFIX))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                repository.count(),
                ((List<KubernetesCluster>) new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<List<KubernetesCluster>>() {})).size());
    }

    @Test
    public void shouldFetchKubernetesClusterByName() throws Exception {
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(initNewKubernetesCluster(NEW_KUBERNETES_CLUSTER_NAME)))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        MvcResult result = mvc.perform(get(URL_PREFIX + "/{name}", NEW_KUBERNETES_CLUSTER_NAME))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                NEW_KUBERNETES_CLUSTER_NAME,
                ((KubernetesCluster) new ObjectMapper().readValue(result.getResponse().getContentAsString(), new TypeReference<KubernetesCluster>() {})).getName());
    }

    @Test
    public void shouldNotFetchNotExistingKubernetesClusterByName() throws Exception {
        mvc.perform(get(URL_PREFIX + "/{name}", DIFFERENT_KUBERNETES_CLUSTER_NAME))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldMapKubernetesClusterToKubernetesClusterView() throws UnknownHostException {
        KubernetesCluster source = initNewKubernetesCluster("k8s1");
        KubernetesClusterView output = modelMapper.map(source, KubernetesClusterView.class);
        assertThat(output.getName(), equalTo(source.getName()));
        assertThat(output.getHelmHostAddress().getHostAddress(), equalTo(source.getHelmHostAddress().getHostAddress()));
        assertThat(output.getRestApiHostAddress().getHostAddress(), equalTo(source.getRestApiHostAddress().getHostAddress()));
        assertThat(output.getRestApiPort(), equalTo(source.getRestApiPort()));
    }

    private KubernetesCluster initNewKubernetesCluster(String name) throws UnknownHostException {
        KubernetesCluster cluster = new KubernetesCluster();
        cluster.setName(name);
        cluster.setHelmHostAddress(InetAddress.getByName("192.168.0.1"));
        cluster.setHelmHostSshUsername("testuser");
        cluster.setHelmHostChartsDirectory("/home/testuser/charts");
        cluster.setRestApiHostAddress(InetAddress.getByName("192.168.0.8"));
        cluster.setRestApiPort(9999);
        KubernetesClusterAttachPoint attachPoint = new KubernetesClusterAttachPoint();
        attachPoint.setRouterId("172.0.0.1");
        attachPoint.setRouterInterfaceName("ge-0/0/1");
        attachPoint.setRouterName("R1");
        cluster.setAttachPoint(attachPoint);
        ExternalNetworkSpec externalNetwork1 = new ExternalNetworkSpec();
        externalNetwork1.setExternalIp(InetAddress.getByName("10.0.0.1"));
        externalNetwork1.setExternalNetwork(InetAddress.getByName("10.0.0.0"));
        externalNetwork1.setExternalNetworkMaskLength(24);
        ExternalNetworkSpec externalNetwork2 = new ExternalNetworkSpec();
        externalNetwork2.setExternalIp(InetAddress.getByName("10.0.1.1"));
        externalNetwork2.setExternalNetwork(InetAddress.getByName("10.0.1.0"));
        externalNetwork2.setExternalNetworkMaskLength(24);
        cluster.setExternalNetworks(Arrays.asList(externalNetwork1, externalNetwork2));
        return cluster;
    }

}
