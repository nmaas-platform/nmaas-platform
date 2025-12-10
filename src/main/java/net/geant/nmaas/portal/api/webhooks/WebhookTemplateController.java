package net.geant.nmaas.portal.api.webhooks;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.AutoWebhookTemplateService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/webhook-templates")
@RequiredArgsConstructor
@Tag(name = "Webhook-templates", description = "Webhooks templates get API")
public class WebhookTemplateController {

    private final AutoWebhookTemplateService templateService;

    @GetMapping("/variables/{eventType}")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    public Set<String> getVariables(@PathVariable WebhookEventType eventType) {
        return templateService.getAvailableVariables(eventType);
    }

    @GetMapping("/default/{eventType}")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    public String getDefault(@PathVariable WebhookEventType eventType) {
        return templateService.getDefaultTemplate(eventType);
    }
}