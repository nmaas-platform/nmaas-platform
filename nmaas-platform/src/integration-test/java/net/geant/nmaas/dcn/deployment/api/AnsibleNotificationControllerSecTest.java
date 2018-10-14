package net.geant.nmaas.dcn.deployment.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter;
import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import net.geant.nmaas.portal.BaseControllerTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource("classpath:application-test-compose.properties")
public class AnsibleNotificationControllerSecTest extends BaseControllerTest {

    @Before
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
