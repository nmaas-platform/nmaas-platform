package net.geant.nmaas.dcn.deployment.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.*;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.nmservice.deployment.repository.DockerHostNetworkRepository;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidClientIdException;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnsiblePlaybookStatusNotificationTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private DockerHostNetworkRepository dockerHostNetworkRepository;

    private final Identifier deploymentId = Identifier.newInstance("this-is-example-deployment-id");
    private final Identifier clientId = Identifier.newInstance("this-is-example-client-id");
    private final Identifier applicationId = Identifier.newInstance("this-is-example-application-id");
    private final String dcnName = "this-is-example-dcn-name";
    private String statusUpdateJsonContent;
    private MockMvc mvc;

    @Before
    public void setUp() throws JsonProcessingException, InvalidDeploymentIdException, InvalidClientIdException {
        appDeploymentRepository.save(new AppDeployment(deploymentId, clientId, applicationId));
        DcnSpec spec = new DcnSpec(dcnName, clientId);
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(spec));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, clientId, DcnDeploymentState.DEPLOYMENT_INITIATED));
        AnsiblePlaybookExecutionStateListener coordinator = new DcnDeploymentCoordinator(dcnRepositoryManager, null, null, applicationEventPublisher, dockerHostNetworkRepository, null);
        statusUpdateJsonContent = new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success"));
        mvc = MockMvcBuilders.standaloneSetup(new AnsibleNotificationRestController(coordinator)).build();
    }

    @After
    public void cleanup() {
        appDeploymentRepository.deleteAll();
    }

    @Test
    public void testAnsiblePlaybookStatusApiUpdate() throws Exception {
        assertThat(dcnRepositoryManager.loadCurrentState(clientId), is(DcnDeploymentState.DEPLOYMENT_INITIATED));
        mvc.perform(MockMvcRequestBuilders.post("/platform/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter(clientId.value()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJsonContent)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Thread.sleep(200);
        assertThat(dcnRepositoryManager.loadCurrentState(clientId), is(DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED));
        mvc.perform(post("/platform/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter(clientId.value()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJsonContent)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Thread.sleep(200);
        assertThat(dcnRepositoryManager.loadCurrentState(clientId), anyOf(is(DcnDeploymentState.VERIFICATION_INITIATED), is(DcnDeploymentState.DEPLOYED)));
    }

}
