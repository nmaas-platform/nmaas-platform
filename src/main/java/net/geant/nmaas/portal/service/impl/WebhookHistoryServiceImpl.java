package net.geant.nmaas.portal.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.api.dto.webhooks.WebhookHistoryDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.persistence.entity.WebhookHistory;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.WebhookHistoryRepository;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookHistoryServiceImpl implements WebhookHistoryService {

    private final WebhookHistoryRepository webhookHistoryRepository;
    private final DomainRepository domainRepository;
    private final ModelMapper modelMapper;
    private final JsonMapper jsonMapper;

    @Override
    public void create(WebhookEventDto webhook, Object payload, Integer responseStatus, String responseBody) {
        WebhookHistory webhookHistory = new WebhookHistory();
        webhookHistory.setWebhookEventId(webhook.getId());
        webhookHistory.setEventType(WebhookEventType.from(webhook.getEventType()));
        if (webhook.getDomain() != null) {
            webhookHistory.setDomainCodename(webhook.getDomain().getCodename());
        }
        webhookHistory.setUrl(webhook.getTargetUrl());
        try {
            webhookHistory.setRequestBody(jsonMapper.writeValueAsString(payload));
        } catch (JacksonException e) {
            log.warn("Failed to write webhook request body to json String with error: {}", e.getMessage());
        }
        webhookHistory.setResponseStatus(responseStatus);
        webhookHistory.setResponseBody(responseBody);
        webhookHistory.setExecutionTimestamp(LocalDateTime.now());
        webhookHistoryRepository.save(webhookHistory);
    }

    public WebhookHistoryDto getById(Long id) {
        Optional<WebhookHistory> entity = webhookHistoryRepository.findById(id);
        if (entity.isPresent()) {
            return modelMapper.map(entity.get(), WebhookHistoryDto.class);
        } else {
            throw new MissingElementException("Resources Limit not found");
        }
    }

    @Override
    public List<WebhookHistoryDto> search(Long webhookEventId, WebhookEventType eventType, String domainCodename,
                                          LocalDateTime from, LocalDateTime to) {
        final Specification<WebhookHistory> spec = prepareQuerySpec(webhookEventId, eventType, domainCodename, from, to);

        return webhookHistoryRepository.findAll(spec)
                .stream()
                .map(entity -> modelMapper.map(entity, WebhookHistoryDto.class))
                .toList();
    }

    @Override
    @Transactional
    public Page<WebhookHistoryDto> search(
            Long webhookEventId,
            WebhookEventType eventType,
            String domainCodename,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        final Specification<WebhookHistory> spec =
                prepareQuerySpec(webhookEventId, eventType, domainCodename, from, to);

        return webhookHistoryRepository.findAll(spec, pageable)
                .map(entity -> modelMapper.map(entity, WebhookHistoryDto.class));
    }

    @Override
    public List<WebhookHistoryDto> search(Long webhookEventId, WebhookEventType eventType, Long domainId,
                                          LocalDateTime from, LocalDateTime to) {
        final Domain domain = domainRepository.findById(domainId).orElseThrow();
        final Specification<WebhookHistory> spec = prepareQuerySpec(webhookEventId, eventType, domain.getCodename(), from, to);

        return webhookHistoryRepository.findAll(spec)
                .stream()
                .map(entity -> modelMapper.map(entity, WebhookHistoryDto.class))
                .toList();
    }

    @Override
    public Page<WebhookHistoryDto> search(Long webhookEventId, WebhookEventType eventType, Long domainId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        final Domain domain = domainRepository.findById(domainId).orElseThrow();
        final Specification<WebhookHistory> spec = prepareQuerySpec(webhookEventId, eventType, domain.getCodename(), from, to);

        return webhookHistoryRepository.findAll(spec, pageable)
                .map(entity -> modelMapper.map(entity, WebhookHistoryDto.class));
    }

    private static Specification<WebhookHistory> prepareQuerySpec(Long eventId, WebhookEventType eventType, String domainCodename, LocalDateTime from, LocalDateTime to) {
        Specification<WebhookHistory> spec = (root, query, cb) -> cb.conjunction();

        if (eventId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("webhookEventId"), eventId));
        }
        if (eventType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("eventType"), eventType));
        }
        if (domainCodename != null && !domainCodename.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("domainCodename"), domainCodename));
        }
        if (from != null && to != null) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("executionTimestamp"), from, to));
        } else if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("executionTimestamp"), from));
        } else if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("executionTimestamp"), to));
        }
        return spec;
    }

}
