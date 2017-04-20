package net.geant.nmaas.dcn.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.api.AnsibleNotificationRestController;
import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.repositories.DcnRepository;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import net.geant.nmaas.externalservices.inventory.vpnconfigs.AnsiblePlaybookVpnConfigRepository;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkDetails;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.network.ContainerNetworkIpamSpec;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnsiblePlaybookStatusNotificationTest {

    @Mock
    private DockerHostRepository dockerHostRepository;

    @Autowired
    private DcnRepository dcnRepository;

    @Autowired
    private AnsiblePlaybookVpnConfigRepository vpnConfigRepository;

    @Autowired
    private DeploymentIdToDcnNameMapper deploymentIdMapper;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private final Identifier deploymentId = Identifier.newInstance("this-is-example-dcn-id");

    private final Identifier clientId = Identifier.newInstance("this-is-example-client-id");

    private final String dcnName = "this-is-example-dcn-name";

    private String statusUpdateJsonContent;

    private MockMvc mvc;

    @Before
    public void setUp() throws JsonProcessingException, DeploymentIdToDcnNameMapper.EntryNotFoundException, DcnRepository.DcnNotFoundException {
        deploymentIdMapper.storeMapping(deploymentId, dcnName);
        DcnSpec spec = new DcnSpec(dcnName, clientId);
        ContainerNetworkDetails containerNetworkDetails =
                new ContainerNetworkDetails(8080, new ContainerNetworkIpamSpec("", ""), 505);
        spec.setNmServiceDeploymentNetworkDetails(containerNetworkDetails);
        dcnRepository.storeNetwork(new DcnInfo(spec));
        dcnRepository.notifyStateChange(new DcnDeploymentStateChangeEvent(this,deploymentId, DcnDeploymentState.DEPLOYMENT_INITIATED));
        AnsiblePlaybookExecutionStateListener coordinator = new DcnDeploymentCoordinator(dockerHostRepository, dcnRepository, deploymentIdMapper, vpnConfigRepository, applicationEventPublisher);
        statusUpdateJsonContent = new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success"));
        mvc = MockMvcBuilders.standaloneSetup(new AnsibleNotificationRestController(coordinator)).build();
    }

    @Test
    public void testAnsiblePlaybookStatusApiUpdate() throws Exception {
        assertThat(dcnRepository.loadCurrentState(dcnName), is(DcnDeploymentState.DEPLOYMENT_INITIATED));
        mvc.perform(post("/platform/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter(dcnName))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJsonContent)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Thread.sleep(100);
        assertThat(dcnRepository.loadCurrentState(dcnName), is(DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED));
        mvc.perform(post("/platform/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter(dcnName))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJsonContent)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Thread.sleep(100);
        assertThat(dcnRepository.loadCurrentState(dcnName), is(DcnDeploymentState.DEPLOYED));
    }

}
