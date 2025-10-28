package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.domain.WebhookEventDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.security.EncryptionService;
import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.persistence.repositories.WebhookEventRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private static final String WEBHOOK_EVENT_NOT_FOUND = "WebhookEvent not found.";

    private final WebhookEventRepository webhookRepository;
    private final EncryptionService encryptionService;
    private final ModelMapper modelMapper;

    public WebhookEvent create(WebhookEventDto webhookEventDto) throws GeneralSecurityException {
        WebhookEvent webhookEvent = new WebhookEvent();
        setWebhookEvent(webhookEvent, webhookEventDto);
        return webhookRepository.save(webhookEvent);
    }

    public WebhookEventDto update(WebhookEventDto webhookEventDto) throws GeneralSecurityException {
        WebhookEvent webhookEvent = webhookRepository.findById(webhookEventDto.getId())
                .orElseThrow(() -> new MissingElementException(WEBHOOK_EVENT_NOT_FOUND));
        setWebhookEvent(webhookEvent, webhookEventDto);
        webhookEvent = webhookRepository.save(webhookEvent);
        WebhookEventDto dto = modelMapper.map(webhookEvent, WebhookEventDto.class);
        dto.setTokenValue(webhookEvent.getTokenValue() == null ? null : encryptionService.decrypt(webhookEvent.getTokenValue()));
        return dto;
    }

    private void setWebhookEvent(WebhookEvent webhookEvent, WebhookEventDto webhookEventDto) throws GeneralSecurityException {
        webhookEvent.setName(webhookEventDto.getName());
        webhookEvent.setTargetUrl(webhookEventDto.getTargetUrl());
        webhookEvent.setEventType(webhookEventDto.getEventType());
        webhookEvent.setTokenValue(webhookEventDto.getTokenValue() == null ? null : encryptionService.encrypt(webhookEventDto.getTokenValue()));
        webhookEvent.setAuthorizationHeader(webhookEventDto.getAuthorizationHeader());
    }

    public void remove(Long id) {
        WebhookEvent webhookEvent = webhookRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(WEBHOOK_EVENT_NOT_FOUND));
        webhookRepository.delete(webhookEvent);
    }

    public List<WebhookEventDto> getAllWebhooks() {
        return webhookRepository.findAll().stream()
                .map(x -> {
                    try {
                        WebhookEventDto dto = modelMapper.map(x, WebhookEventDto.class);
                        dto.setTokenValue(x.getTokenValue() == null ? null : encryptionService.decrypt(x.getTokenValue()));
                        return dto;
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    public List<Long> findIdByEventType(WebhookEventType webhookEventType) {
        return webhookRepository.findIdByEventType(webhookEventType);
    }

    public WebhookEventDto getById(Long id) throws GeneralSecurityException {
        WebhookEvent event = webhookRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(String.format("WebhookEventType with id: %d cannot be found", id)));
        WebhookEventDto dto = modelMapper.map(event, WebhookEventDto.class);
        dto.setTokenValue(event.getTokenValue() == null ? null : encryptionService.decrypt(event.getTokenValue()));
        return dto;
    }
}
