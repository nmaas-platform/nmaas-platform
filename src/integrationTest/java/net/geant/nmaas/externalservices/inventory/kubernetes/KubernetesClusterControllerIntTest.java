package net.geant.nmaas.externalservices.inventory.kubernetes;

import net.geant.nmaas.portal.api.market.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class KubernetesClusterControllerIntTest {

    private final static String URL_PREFIX = "/api/management/kubernetes";

    private final static String KUBERNETES_CLUSTER_JSON =
                "{" +
                    "\"ingress\":{" +
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
                    "\"deployment\":{" +
                        "\"namespaceConfigOption\":\"USE_DOMAIN_NAMESPACE\"," +
                        "\"defaultNamespace\":\"test-namespace\"," +
                        "\"defaultStorageClass\":\"storageClass\"," +
                        "\"smtpServerHostname\":\"test-postfix\"," +
                        "\"smtpServerPort\":587," +
                        "\"smtpServerUsername\":\"\"," +
                        "\"smtpServerPassword\":\"\"," +
                        "\"forceDedicatedWorkers\":false" +
                    "}" +
                "}";

    @Autowired
    private KubernetesClusterDeploymentManager clusterDeploymentManager;

    @Autowired
    private KubernetesClusterIngressManager clusterIngressManager;

    @Test
    public void shouldFetchKubernetesCluster() throws Exception {
        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new KubernetesClusterController(clusterIngressManager, clusterDeploymentManager))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        MvcResult result = mvc.perform(get(URL_PREFIX))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(KUBERNETES_CLUSTER_JSON, result.getResponse().getContentAsString());
    }

}
