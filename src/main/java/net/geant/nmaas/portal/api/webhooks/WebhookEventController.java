package net.geant.nmaas.portal.api.webhooks;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.WebhookEventDto;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.WebhookEvent;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/webhooks")
@Slf4j
@RequiredArgsConstructor
public class WebhookEventController {

    private static final String UNABLE_TO_CHANGE_WEBHOOK_EVENT = "Unable to change WebhookEvent";

    private final WebhookEventService webhookEventService;

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<Id> createWebhook(@RequestBody @Valid WebhookEventDto webhook) {
        WebhookEvent webhookEvent = null;
        try {
            webhookEvent = webhookEventService.create(webhook);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(new Id(webhookEvent.getId()));
    }

    @PutMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<WebhookEventDto> updateWebhook(@PathVariable Long id, @RequestBody @Valid WebhookEventDto webhook) {
        if (!id.equals(webhook.getId())) {
            throw new ProcessingException(UNABLE_TO_CHANGE_WEBHOOK_EVENT);
        }

        try {
            return ResponseEntity.ok(webhookEventService.update(webhook));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public void deleteWebhook(@PathVariable Long id) {
        webhookEventService.remove(id);
    }

    @GetMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<WebhookEventDto> getWebhook(@PathVariable Long id) throws GeneralSecurityException {
        return ResponseEntity.ok(webhookEventService.getById(id));

    }

    @GetMapping
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<WebhookEventDto>> getAllWebhooks() {
        return ResponseEntity.ok(webhookEventService.getAllWebhooks());
    }
}
