package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoWebhookTemplateServiceTest {

    private final AutoWebhookTemplateService templateService = new AutoWebhookTemplateService();

    @BeforeEach
    void setUp() {
        templateService.init();
    }

    @Test
    void shouldAcceptValidTemplateWithKnownVariables() {
        String template = """
                {
                  "deployment": {
                    "id": $APPDEPLOYMENT_DEPLOYMENTID,
                    "name": $APPDEPLOYMENT_DEPLOYMENTNAME
                  },
                  "type": $WEBHOOKEVENTTYPE
                }
                """;

        assertDoesNotThrow(() -> templateService.validateTemplate(template, WebhookEventType.APPLICATION_DEPLOYMENT));
    }

    @Test
    void shouldAcceptBlankOrMissingTemplate() {
        assertDoesNotThrow(() -> templateService.validateTemplate(null, WebhookEventType.DOMAIN_ACTION));
        assertDoesNotThrow(() -> templateService.validateTemplate("  ", WebhookEventType.DOMAIN_ACTION));
    }

    @Test
    void shouldRejectTemplateWithInvalidJsonSyntax() {
        String template = """
                {
                  "domain": "id": $DOMAINVIEW_ID,
                  "type": $WEBHOOKEVENTTYPE
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.validateTemplate(template, WebhookEventType.DOMAIN_ACTION)
        );

        assertEquals("Template JSON Syntax Check failed", exception.getMessage());
    }

    @Test
    void shouldRejectTemplateWithVariableNotAvailableForEventType() {
        String template = """
                {
                  "domain": {
                    "id": $DOMAINVIEW_ID,
                    "description": $DOMAINVIEW_DESCR
                  },
                  "type": $WEBHOOKEVENTTYPE
                }
                """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.validateTemplate(template, WebhookEventType.DOMAIN_ACTION)
        );

        assertEquals("$DOMAINVIEW_DESCR is not a valid variable for this webhook", exception.getMessage());
    }

    @Test
    void shouldExposeVariablesUsedByValidation() {
        Set<String> variables = templateService.getAvailableVariables(WebhookEventType.APPLICATION_DEPLOYMENT);

        assertTrue(variables.contains("$APPDEPLOYMENT_DEPLOYMENTID"));
        assertTrue(variables.contains("$APPDEPLOYMENT_DEPLOYMENTNAME"));
        assertTrue(variables.contains("$WEBHOOKEVENTTYPE"));
    }
}
