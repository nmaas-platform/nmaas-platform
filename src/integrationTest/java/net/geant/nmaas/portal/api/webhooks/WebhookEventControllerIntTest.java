package net.geant.nmaas.portal.api.webhooks;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.api.dto.webhooks.WebhookEventTypeDto;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.service.AclService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class WebhookEventControllerIntTest extends BaseControllerTestSetup {

    @MockitoBean
    private WebhookEventService webhookEventService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AclService aclService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mvc = createMVC();
    }

    @Test
    void shouldCreateWebhookAsSystemAdmin() throws Exception {
        WebhookEvent webhookEvent = new WebhookEvent();
        webhookEvent.setId(44L);
        when(webhookEventService.create(any(WebhookEventDto.class))).thenReturn(webhookEvent);

        mvc.perform(post("/api/webhooks")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookEventDto(null, null))))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":44}"));
    }

    @Test
    void shouldReturnNotAcceptableWhenWebhookIdsDoNotMatch() throws Exception {
        mvc.perform(put("/api/webhooks/100")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookEventDto(99L, null))))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void shouldCreateWebhookInDomainForAuthorizedDomainOwner() throws Exception {
        WebhookEvent webhookEvent = new WebhookEvent();
        webhookEvent.setId(88L);
        when(webhookEventService.create(any(WebhookEventDto.class))).thenReturn(webhookEvent);
        when(userService.findByUsername(UsersHelper.DOMAIN1_ADMIN.getUsername())).thenReturn(Optional.of(UsersHelper.DOMAIN1_ADMIN));
        when(aclService.isAuthorized(eq(UsersHelper.DOMAIN1_ADMIN.getId()), eq(2L), eq("domain"), any())).thenReturn(true);

        mvc.perform(post("/api/webhooks/domain/2")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.DOMAIN1_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookEventDto(null, domain(2L)))))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":88}"));

        verify(webhookEventService).create(any(WebhookEventDto.class));
    }

    @Test
    void shouldReturnBadRequestWhenWebhookDomainDoesNotMatchPath() throws Exception {
        mvc.perform(post("/api/webhooks/domain/2")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(webhookEventDto(null, domain(3L)))))
                .andExpect(status().isBadRequest());
    }

    private static WebhookEventDto webhookEventDto(Long id, DomainBaseDto domain) {
        WebhookEventDto dto = new WebhookEventDto(id, "webhook", "https://example.test/webhook", WebhookEventTypeDto.APPLICATION_DEPLOYMENT);
        dto.setDomain(domain);
        return dto;
    }

    private static DomainBaseDto domain(Long id) {
        DomainBaseDto dto = new DomainBaseDto();
        dto.setId(id);
        dto.setCodename("D" + id);
        dto.setName("domain-" + id);
        return dto;
    }
}
