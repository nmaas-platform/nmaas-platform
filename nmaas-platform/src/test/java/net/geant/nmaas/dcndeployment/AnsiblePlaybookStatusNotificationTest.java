package net.geant.nmaas.dcndeployment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcndeployment.api.AnsibleNotificationRestController;
import net.geant.nmaas.dcndeployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.dcndeployment.repository.DcnRepository;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Mock
    private DcnRepository dcnRepository;

    private final String dcnName = "this-is-example-dcn-name";

    private String statusUpdateJsonContent;

    private MockMvc mvc;

    @Before
    public void setUp() throws JsonProcessingException {
        DcnDeploymentCoordinator coordinator = new DcnDeploymentCoordinator(dockerHostRepository, dcnRepository);
        mvc = MockMvcBuilders.standaloneSetup(new AnsibleNotificationRestController(coordinator)).build();
        statusUpdateJsonContent = new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success"));
    }

    @Test
    public void testFakeAnsibleDeployAndStatusUpdate() throws Exception {
        mvc.perform(get("/api/dcns"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/dcns/notifications/{serviceId}/status", DcnIdentifierConverter.encode(dcnName))
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusUpdateJsonContent)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        ArgumentCaptor<String> dcnNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<DcnDeploymentState> dcnStateCaptor = ArgumentCaptor.forClass(DcnDeploymentState.class);

        verify(dcnRepository, times(1)).updateDcnState(dcnNameCaptor.capture(), dcnStateCaptor.capture());
        assertThat(dcnNameCaptor.getValue(), equalTo(dcnName));
        assertThat(dcnStateCaptor.getValue(), equalTo(DcnDeploymentState.CONFIGURED));
    }

}
