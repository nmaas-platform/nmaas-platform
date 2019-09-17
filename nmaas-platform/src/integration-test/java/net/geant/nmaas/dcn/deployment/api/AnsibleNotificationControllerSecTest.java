package net.geant.nmaas.dcn.deployment.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"env_kubernetes", "dcn_ansible", "db_memory"})
public class AnsibleNotificationControllerSecTest extends BaseControllerTestSetup {

    @BeforeEach
    public void setup() {
        createMVC();
    }

    @Test
    public void shouldAuthAndCallNotificationPost() throws Exception {
        mvc.perform(post("/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter("testDcn"))
                .with(httpBasic(context.getEnvironment().getProperty("ansible.notification.client.username"), context.getEnvironment().getProperty("ansible.notification.client.password")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success")))
                .accept(MediaType.APPLICATION_JSON))
        		.andExpect(status().isCreated());
    }

    @Test
    public void shouldAuthAndForbidNotificationPost() throws Exception {
        mvc.perform(post("/api/dcns/notifications/{serviceId}/status", AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter("testDcn"))
                .with(httpBasic("testClient", "testPassword"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new AnsiblePlaybookStatus("success")))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}
