package net.geant.nmaas.nmservice.configuration.api;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class GitLabWebhookControllerSecTest extends BaseControllerTestSetup {

    @BeforeEach
    public void setup(){
        createMVC();
    }

    @Test
    public void shouldAuthorizeSinceNoAuthInPlace() throws Exception{
        mvc.perform(post("/api/gitlab/webhooks/1"))
                .andExpect(status().isOk());
    }

}
