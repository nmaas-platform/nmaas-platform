package net.geant.nmaas.kubernetes;

import net.geant.nmaas.kubernetes.api.KubernetesClusterController;
import net.geant.nmaas.portal.api.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class KubernetesClusterControllerIntTest {

    private static final String URL_PREFIX = "/api/management/kubernetes";
    private static final String KUBERNETES_CLUSTER_JSON =
                "{" +
                      "\"ingress\":{\"id\":null," +
                        "\"controllerConfigOption\":\"USE_EXISTING\"," +
                        "\"supportedIngressClass\":\"ingress-class\"," +
                        "\"publicIngressClass\":\"public\"," +
                        "\"controllerChartName\":\"nginx\"," +
                        "\"controllerChartArchive\":\"chart.tgz\"," +
                        "\"resourceConfigOption\":\"DEPLOY_FROM_CHART\"," +
                        "\"externalServiceDomain\":\"test.net\"," +
                        "\"publicServiceDomain\":\"public.test.net\"," +
                        "\"tlsSupported\":true," +
                        "\"certificateConfigOption\":\"USE_LETSENCRYPT\"," +
                        "\"issuerOrWildcardName\":\"test-issuer\"," +
                        "\"ingressPerDomain\":true" +
                        "}," +
                     "\"deployment\":{\"id\":null," +
                        "\"namespaceConfigOption\":\"USE_DOMAIN_NAMESPACE\"," +
                        "\"defaultNamespace\":\"test-namespace\"," +
                        "\"defaultStorageClass\":\"storageClass\"," +
                        "\"smtpServerHostname\":\"test-postfix\"," +
                        "\"smtpServerPort\":587," +
                        "\"smtpServerUsername\":\"\"," +
                        "\"smtpServerPassword\":\"\"," +
                        "\"smtpFromDefaultDomain\":\"\"," +
                        "\"forceDedicatedWorkers\":false" +
                        "}" +
                "}";

    private final KubernetesClusterDeploymentManager clusterDeploymentManager;
    private final KubernetesClusterIngressManager clusterIngressManager;
    private final JsonMapper jsonMapper;

    public KubernetesClusterControllerIntTest(@Autowired KubernetesClusterDeploymentManager clusterDeploymentManager,
                                              @Autowired KubernetesClusterIngressManager clusterIngressManager,
                                              @Autowired JsonMapper jsonMapper) {
        this.clusterDeploymentManager = clusterDeploymentManager;
        this.clusterIngressManager = clusterIngressManager;
        this.jsonMapper = jsonMapper;
    }

    @Test
    void shouldFetchKubernetesCluster() throws Exception {
        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new KubernetesClusterController(clusterIngressManager, clusterDeploymentManager))
                .setControllerAdvice(new ApiExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter(jsonMapper))
                .build();

        MvcResult result = mvc.perform(get(URL_PREFIX))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(KUBERNETES_CLUSTER_JSON, result.getResponse().getContentAsString());
    }

}
