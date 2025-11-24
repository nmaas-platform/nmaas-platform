package net.geant.nmaas.portal.api.webhooks;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.domain.WebhookHistoryDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/webhooks-history")
@RequiredArgsConstructor
@Tag(name = "Webhook history", description = "Webhook history management API")
public class WebhookHistoryController {

    private final WebhookHistoryService webhookHistoryService;

    @GetMapping("/{id}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<WebhookHistoryDto> getWebhook(@PathVariable Long id) {
        return ResponseEntity.ok(webhookHistoryService.getById(id));
    }

    @GetMapping
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public ResponseEntity<List<WebhookHistoryDto>> search(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) WebhookEventType eventType,
            @RequestParam(required = false) String domainCodename,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(webhookHistoryService.search(eventId, eventType, domainCodename, from, to));
    }

    @GetMapping("/domain/{domainId}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasPermission(#domainId, 'domain', 'OWNER')")
    public ResponseEntity<List<WebhookHistoryDto>> searchInDomain(
            @PathVariable Long domainId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) WebhookEventType eventType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(webhookHistoryService.search(eventId, eventType, domainId, from, to));
    }

}
