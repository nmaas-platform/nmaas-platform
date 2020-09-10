package net.geant.nmaas.portal.api.shell;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.shell.connectors.KubernetesConnectorHelper;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ShellClientControllerIntTest extends BaseControllerTestSetup {

    @MockBean
    private ShellSessionsStorage storage;

    @MockBean
    private KubernetesConnectorHelper connectorHelper;

    @BeforeEach
    void setup(){
        this.mvc = createMVC();
        Map<String, String> podNames = new HashMap<>();
        podNames.put("name1", "displayName1");
        podNames.put("name2", "displayName2");
        when(connectorHelper.getPodNamesForAppInstance(1L)).thenReturn(podNames);
    }

    @Test
    public void shouldRetrievePodNames() throws Exception {
        when(connectorHelper.checkAppInstanceSupportsSshAccess(1L)).thenReturn(true);
        MvcResult result = mvc.perform(get("/api/shell/{id}/podnames", 1L)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("name1"));
    }

    @Test
    public void shouldNotRetrievePodNames() throws Exception {
        when(connectorHelper.checkAppInstanceSupportsSshAccess(1L)).thenReturn(false);
        mvc.perform(get("/api/shell/{id}/podnames", 1L)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable())
                .andReturn();
    }

}
