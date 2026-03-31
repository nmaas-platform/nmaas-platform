package net.geant.nmaas.portal.api.webhooks;

import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.AutoWebhookTemplateService;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WebhookTemplateControllerTest {

    private final AutoWebhookTemplateService templateService = mock(AutoWebhookTemplateService.class);
    private final WebhookTemplateController controller = new WebhookTemplateController(templateService);

    @Test
    void shouldGetVariables() {
        Set<String> variables = Set.of("$WEBHOOKEVENTTYPE", "$ACTION");
        when(templateService.getAvailableVariables(WebhookEventType.DOMAIN_ACTION)).thenReturn(variables);

        Set<String> result = controller.getVariables(WebhookEventType.DOMAIN_ACTION);

        assertSame(variables, result);
        verify(templateService).getAvailableVariables(WebhookEventType.DOMAIN_ACTION);
    }

    @Test
    void shouldGetDefaultTemplate() {
        String template = "{\"type\": $WEBHOOKEVENTTYPE}";
        when(templateService.getDefaultTemplate(WebhookEventType.APPLICATION_DEPLOYMENT)).thenReturn(template);

        String result = controller.getDefault(WebhookEventType.APPLICATION_DEPLOYMENT);

        assertEquals(template, result);
        verify(templateService).getDefaultTemplate(WebhookEventType.APPLICATION_DEPLOYMENT);
    }
}
