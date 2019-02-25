package net.geant.nmaas.dcn.deployment.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.AnsibleDcnDeploymentExecutor;
import net.geant.nmaas.dcn.deployment.AnsiblePlaybookExecutionStateListener;
import net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter;
import net.geant.nmaas.dcn.deployment.DcnDeploymentStateChangeEvent;
import net.geant.nmaas.dcn.deployment.DcnRepositoryManager;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.dcn.deployment.entities.DcnSpec;
import net.geant.nmaas.dcn.deployment.repositories.DcnInfoRepository;
import net.geant.nmaas.orchestration.entities.AppDeployment;
import net.geant.nmaas.orchestration.entities.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.orchestration.repositories.AppDeploymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"env_kubernetes", "dcn_ansible", "db_memory"})
public class AnsibleNotificationControllerIntTest {

    @Autowired
    private DcnRepositoryManager dcnRepositoryManager;
    @Autowired
    private DcnInfoRepository dcnInfoRepository;
    @Autowired
    private AppDeploymentRepository appDeploymentRepository;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private static final String DOMAIN = "domain";
    private static final String DEPLOYMENT_NAME = "deploymentName";
    private static final String DCN_NAME = "this-is-example-dcn-name";
    private final Identifier deploymentId = Identifier.newInstance("this-is-example-deployment-id");
    private final Identifier applicationId = Identifier.newInstance("this-is-example-application-id");
    private String statusUpdateJsonContent;
    private MockMvc mvc;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        AppDeployment appDeployment = AppDeployment.builder()
                .deploymentId(deploymentId)
                .domain(DOMAIN)
                .applicationId(applicationId)
                .deploymentName(DEPLOYMENT_NAME)
                .configFileRepositoryRequired(true)
                .storageSpace(20)
                .build();
        appDeploymentRepository.save(appDeployment);
        DcnSpec spec = new DcnSpec(DCN_NAME, DOMAIN);
        dcnRepositoryManager.storeDcnInfo(new DcnInfo(spec));
        dcnRepositoryManager.notifyStateChange(new DcnDeploymentStateChangeEvent(this, DOMAIN, DcnDeploymentState.DEPLOYMENT_INITIATED));
        AnsiblePlaybookExecutionStateListener coordinator = new AnsibleDcnDeploymentExecutor(dcnRepositoryManager, applicationEventPublisher, null, null, null);
        statusUpdateJsonContent = new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success"));
        mvc = MockMvcBuilders.standaloneSetup(new AnsibleNotificationController(coordinator)).build();
    }

    @AfterEach
    public void cleanup() throws InvalidDomainException {
        appDeploymentRepository.deleteAll();
        dcnInfoRepository.deleteAll();
    }

    @Test
    public void testAnsiblePlaybookStatusApiUpdate() throws Exception {
        assertThat(dcnRepositoryManager.loadCurrentState(DOMAIN), is(DcnDeploymentState.DEPLOYMENT_INITIATED));
        mvc.perform(MockMvcRequestBuilders.post("/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter(DOMAIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJsonContent)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Thread.sleep(200);
        assertThat(dcnRepositoryManager.loadCurrentState(DOMAIN), is(DcnDeploymentState.ANSIBLE_PLAYBOOK_CONFIG_FOR_CLIENT_SIDE_ROUTER_COMPLETED));
        mvc.perform(post("/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter(DOMAIN))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJsonContent)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        Thread.sleep(200);
        assertThat(dcnRepositoryManager.loadCurrentState(DOMAIN), anyOf(is(DcnDeploymentState.VERIFICATION_INITIATED), is(DcnDeploymentState.DEPLOYED)));
    }

}
