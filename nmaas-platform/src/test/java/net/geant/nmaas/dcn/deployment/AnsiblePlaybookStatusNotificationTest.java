package net.geant.nmaas.dcn.deployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.api.AnsibleNotificationRestController;
import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.repository.DcnInfo;
import net.geant.nmaas.dcn.deployment.repository.DcnRepository;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
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

    @Mock
    private DeploymentIdToDcnNameMapper deploymentIdMapper;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private final String dcnName = "this-is-example-dcn-name";

    private String statusUpdateJsonContent;

    private MockMvc mvc;

    @Before
    public void setUp() throws JsonProcessingException {
        dcnRepository.storeNetwork(new DcnInfo(dcnName, DcnDeploymentState.DEPLOYMENT_INITIATED, null));
        AnsiblePlaybookExecutionStateListener coordinator = new DcnDeploymentCoordinator(dockerHostRepository, dcnRepository, deploymentIdMapper, applicationEventPublisher);
        mvc = MockMvcBuilders.standaloneSetup(new AnsibleNotificationRestController(coordinator)).build();
        statusUpdateJsonContent = new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success"));
    }

    @Test
    public void testAnsiblePlaybookStatusApiUpdate() throws Exception {
        assertThat(dcnRepository.loadCurrentState(dcnName), is(DcnDeploymentState.DEPLOYMENT_INITIATED));
        mvc.perform(post("/platform/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter(dcnName))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJsonContent)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(dcnRepository.loadCurrentState(dcnName), is(DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED));
        mvc.perform(post("/platform/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter(dcnName))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJsonContent)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        assertThat(dcnRepository.loadCurrentState(dcnName), is(DcnDeploymentState.DEPLOYED));
    }

}
