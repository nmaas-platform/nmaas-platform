package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.domain.WebhookEventDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.security.EncryptionService;
import net.geant.nmaas.portal.persistent.entity.WebhookEvent;
import net.geant.nmaas.portal.persistent.entity.WebhookEventType;
import net.geant.nmaas.portal.persistent.repositories.WebhookEventRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Stream;

@Service
public class WebhookEventService {

    private final WebhookEventRepository webhookRepository;
    private final EncryptionService encryptionService;
    private final ModelMapper modelMapper;
    private static final String WEBHOOK_EVENT_NOT_FOUND = "WebhookEvent not found.";

    @Autowired
    public WebhookEventService(WebhookEventRepository webhookRepository, EncryptionService encryptionService, ModelMapper modelMapper) {
        this.webhookRepository = webhookRepository;
        this.encryptionService = encryptionService;
        this.modelMapper = modelMapper;
    }

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

    public Stream<Long> findIdByEventType(WebhookEventType webhookEventType) {
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
