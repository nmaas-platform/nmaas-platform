package net.geant.nmaas.portal.api.webhooks;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/${nmaas.api.version:v1}/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhooks management API")
public class WebhookEventController {

    private static final String UNABLE_TO_CHANGE_WEBHOOK_EVENT = "Unable to change WebhookEvent";

    private final WebhookEventService webhookEventService;

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<Id> createWebhook(@RequestBody @Valid WebhookEventDto webhook) {
        try {
            final WebhookEvent webhookEvent = webhookEventService.create(webhook);
            return ResponseEntity.ok(new Id(webhookEvent.getId()));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
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
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public ResponseEntity<WebhookEventDto> getWebhook(@PathVariable Long id) throws GeneralSecurityException {
        return ResponseEntity.ok(webhookEventService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public ResponseEntity<List<WebhookEventDto>> getAllWebhooks() {
        return ResponseEntity.ok(webhookEventService.getAllWebhooks());
    }


    @PostMapping("/domain/{domainId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public ResponseEntity<Id> createWebhookInDomain(@PathVariable Long domainId, @RequestBody @Valid WebhookEventDto webhook) {
        if (!domainId.equals(webhook.getDomain().getId())) {
            throw new IllegalArgumentException("Domain identifiers don't match.");
        }
        try {
            final WebhookEvent webhookEvent = webhookEventService.create(webhook);
            return ResponseEntity.ok(new Id(webhookEvent.getId()));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/domain/{domainId}/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public ResponseEntity<WebhookEventDto> updateWebhookInDomain(@PathVariable Long domainId, @PathVariable Long id, @RequestBody @Valid WebhookEventDto webhook, Principal principal) {
        if (!id.equals(webhook.getId()) || (!domainId.equals(webhook.getDomain().getId()))) {
            throw new ProcessingException(UNABLE_TO_CHANGE_WEBHOOK_EVENT);
        }
        try {
            return ResponseEntity.ok(webhookEventService.update(domainId, webhook));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/domain/{domainId}/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public void deleteWebhookInDomain(@PathVariable Long domainId, @PathVariable Long id) {
        webhookEventService.remove(domainId, id);
    }

    @GetMapping("/domain/{domainId}/{id}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public ResponseEntity<WebhookEventDto> getWebhookInDomain(@PathVariable Long domainId, @PathVariable Long id) throws GeneralSecurityException {
        return ResponseEntity.ok(webhookEventService.getById(domainId, id));
    }

    @GetMapping("/domain/{domainId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    public ResponseEntity<List<WebhookEventDto>> getWebhooksInDomain(@PathVariable Long domainId) {
        return ResponseEntity.ok(webhookEventService.getAllWebhooks(domainId));
    }

    //-------------------------Pageable


    @GetMapping(params = {"page"})
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public Page<WebhookEventDto> getAllWebhooksPageable(
            @PageableDefault(page = 0, size = 15, sort = "id") Pageable pageable,
            @RequestParam(required = false) String searchValue) {
        return webhookEventService.getAllWebhooks(pageable, searchValue);
    }

    @GetMapping(value = "/domain/{domainId}", params = "page")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    public Page<WebhookEventDto> getWebhooksInDomainPageable(
            @PathVariable Long domainId,
            @PageableDefault(page = 0, size = 15, sort = "id") Pageable pageable,
            @RequestParam(required = false) String searchValue) {
        return webhookEventService.getAllWebhooks(domainId, pageable, searchValue);
    }

}