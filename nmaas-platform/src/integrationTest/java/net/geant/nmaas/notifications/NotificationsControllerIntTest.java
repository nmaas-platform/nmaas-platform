package net.geant.nmaas.notifications;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NotificationsControllerIntTest extends BaseControllerTestSetup {

    @BeforeEach
    public void setup() {
        mvc = createMVC();
    }

    @Test
    public void sendValidContactMail() throws Exception {
        mvc.perform(post("/api/mail?token=mockedCaptcha-notRelevant")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mailType\": \"CONTACT_FORM\",\"otherAttributes\": {\"text\": \"test\",\"TITLE\":\"Message title\"}}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void sendInvalidContactMail() throws Exception {
        mvc.perform(post("/api/mail?token=mockedCaptcha-notRelevant")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mailType\": \"BROADCAST\",\"otherAttributes\": {\"text\": \"test\",\"TITLE\":\"Message title\"}}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void sendValidAdminNotification() throws Exception {
        String token = getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN);
        mvc.perform(post("/api/mail/admin")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mailType\": \"BROADCAST\",\"otherAttributes\": {\"text\": \"test\",\"TITLE\":\"Message title\"}}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }

    @Test
    public void unauthorizedAdminNotificationShouldFail() throws Exception {
        mvc.perform(post("/api/mail/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mailType\": \"BROADCAST\",\"otherAttributes\": {\"text\": \"test\",\"TITLE\":\"Message title\"}}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}
