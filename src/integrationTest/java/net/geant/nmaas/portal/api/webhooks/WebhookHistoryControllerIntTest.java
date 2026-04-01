package net.geant.nmaas.portal.api.webhooks;

import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;
import net.geant.nmaas.api.dto.webhooks.WebhookHistoryDto;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WebhookHistoryControllerIntTest extends BaseControllerTestSetup {

    @MockitoBean
    private WebhookHistoryService webhookHistoryService;

    @BeforeEach
    void setup() {
        mvc = createMVC();
    }

    @Test
    void shouldGetWebhookHistoryAsOperator() throws Exception {
        when(webhookHistoryService.getById(5L)).thenReturn(historyDto(5L, 15L));

        mvc.perform(get("/api/webhooks-history/5")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.OPERATOR))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.webhookEventId").value(15));
    }

    @Test
    void shouldSearchWebhookHistoryWithFilters() throws Exception {
        LocalDateTime from = LocalDateTime.of(2026, 3, 1, 8, 0);
        LocalDateTime to = LocalDateTime.of(2026, 3, 2, 8, 0);
        when(webhookHistoryService.search(15L, WebhookEventType.DOMAIN_ACTION, "D1", from, to))
                .thenReturn(List.of(historyDto(7L, 15L)));

        mvc.perform(get("/api/webhooks-history")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.OPERATOR))
                        .param("eventId", "15")
                        .param("eventType", "DOMAIN_ACTION")
                        .param("domainCodename", "D1")
                        .param("from", "2026-03-01T08:00:00")
                        .param("to", "2026-03-02T08:00:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].eventType").value("DOMAIN_ACTION"));

        verify(webhookHistoryService).search(15L, WebhookEventType.DOMAIN_ACTION, "D1", from, to);
    }

    @Test
    void shouldRejectWebhookHistoryForRegularDomainUser() throws Exception {
        mvc.perform(get("/api/webhooks-history/5")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.DOMAIN1_USER1))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private static WebhookHistoryDto historyDto(Long id, Long eventId) {
        return new WebhookHistoryDto(
                id,
                eventId,
                WebhookEventTypeDto.DOMAIN_ACTION,
                "D1",
                "https://example.test/webhook",
                "{\"domain\":\"D1\"}",
                200,
                "ok",
                LocalDateTime.of(2026, 3, 1, 9, 0)
        );
    }
}
