package net.geant.nmaas.portal.api.webhooks;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.AutoWebhookTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WebhookTemplateControllerIntTest extends BaseControllerTestSetup {

    @MockitoBean
    private AutoWebhookTemplateService templateService;

    @BeforeEach
    void setup() {
        mvc = createMVC();
    }

    @Test
    void shouldGetVariablesAsSystemAdmin() throws Exception {
        when(templateService.getAvailableVariables(WebhookEventType.DOMAIN_ACTION))
                .thenReturn(Set.of("$ACTION", "$WEBHOOKEVENTTYPE"));

        mvc.perform(get("/api/webhook-templates/variables/DOMAIN_ACTION")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    void shouldGetDefaultTemplateAsSystemAdmin() throws Exception {
        when(templateService.getDefaultTemplate(WebhookEventType.APPLICATION_DEPLOYMENT))
                .thenReturn("{\"type\": $WEBHOOKEVENTTYPE}");

        mvc.perform(get("/api/webhook-templates/default/APPLICATION_DEPLOYMENT")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("{\"type\": $WEBHOOKEVENTTYPE}"));
    }

    @Test
    void shouldRejectVariablesForOperator() throws Exception {
        mvc.perform(get("/api/webhook-templates/variables/DOMAIN_ACTION")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.OPERATOR))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
