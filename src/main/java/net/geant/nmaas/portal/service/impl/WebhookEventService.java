package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.api.dto.webhooks.WebhookEventDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.security.EncryptionService;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.persistence.repositories.WebhookEventRepository;
import net.geant.nmaas.portal.service.AutoWebhookTemplateService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private static final String WEBHOOK_EVENT_NOT_FOUND = "WebhookEvent not found.";

    private final WebhookEventRepository webhookRepository;
    private final EncryptionService encryptionService;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final AutoWebhookTemplateService templateService;

    public WebhookEvent create(WebhookEventDto webhookEventDto) throws GeneralSecurityException {
        WebhookEvent webhookEvent = new WebhookEvent();
        templateService.validateTemplate(webhookEventDto.getTemplate(), WebhookEventType.from(webhookEventDto.getEventType()));
        setWebhookEvent(webhookEvent, webhookEventDto);
        return webhookRepository.save(webhookEvent);
    }

    public WebhookEventDto update(WebhookEventDto webhookEventDto) throws GeneralSecurityException {
        WebhookEvent webhookEvent = webhookRepository.findById(webhookEventDto.getId())
                .orElseThrow(() -> new MissingElementException(WEBHOOK_EVENT_NOT_FOUND));
        templateService.validateTemplate(webhookEventDto.getTemplate(), WebhookEventType.from(webhookEventDto.getEventType()));
        setWebhookEvent(webhookEvent, webhookEventDto);
        webhookEvent = webhookRepository.save(webhookEvent);
        return getWebhookEventDto(webhookEvent);
    }

    public WebhookEventDto update(Long domainId, WebhookEventDto webhookEventDto) throws GeneralSecurityException {
        WebhookEvent webhookEvent = webhookRepository.findByIdAndDomain_Id(webhookEventDto.getId(), domainId)
                .orElseThrow(() -> new MissingElementException(WEBHOOK_EVENT_NOT_FOUND));
        if (Objects.isNull(webhookEvent.getDomain()) || !webhookEventDto.getDomain().getId().equals(webhookEvent.getDomain().getId())) {
            throw new IllegalArgumentException("Can't change webhook domain");
        }
        templateService.validateTemplate(webhookEventDto.getTemplate(), WebhookEventType.from(webhookEventDto.getEventType()));
        setWebhookEvent(webhookEvent, webhookEventDto);
        webhookEvent = webhookRepository.save(webhookEvent);
        return getWebhookEventDto(webhookEvent);
    }

    private void setWebhookEvent(WebhookEvent webhookEvent, WebhookEventDto webhookEventDto) throws GeneralSecurityException {
        //domain is combined only with APPLICATION_DEPLOYMENT, APPLICATION_REMOVAL and USER_ASSIGNMENT
        if (webhookEventDto.getDomain() != null
                && Stream.of(WebhookEventType.DOMAIN_ACTION, WebhookEventType.DOMAIN_GROUP_ACTION).anyMatch(x -> WebhookEventType.from(webhookEventDto.getEventType()).equals(x))) {
            throw new IllegalArgumentException("Domain can not combine with " + webhookEventDto.getEventType());
        }

        webhookEvent.setName(webhookEventDto.getName());
        webhookEvent.setTargetUrl(webhookEventDto.getTargetUrl());
        webhookEvent.setEventType(WebhookEventType.from(webhookEventDto.getEventType()));
        webhookEvent.setTokenValue(webhookEventDto.getTokenValue() == null ? null : encryptionService.encrypt(webhookEventDto.getTokenValue()));
        webhookEvent.setAuthorizationHeader(webhookEventDto.getAuthorizationHeader());
        webhookEvent.setDomain(webhookEventDto.getDomain() != null ? new Domain(webhookEventDto.getDomain().getId()) : null);
        webhookEvent.setTemplate(webhookEventDto.getTemplate());
    }

    public void remove(Long id) {
        WebhookEvent webhookEvent = webhookRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(WEBHOOK_EVENT_NOT_FOUND));
        webhookRepository.delete(webhookEvent);
    }

    public void remove(Long domainId, Long id) {
        WebhookEvent webhookEvent = webhookRepository.findByIdAndDomain_Id(id, domainId)
                .orElseThrow(() -> new MissingElementException(WEBHOOK_EVENT_NOT_FOUND));
        webhookRepository.delete(webhookEvent);
    }

    public List<WebhookEventDto> getAllWebhooks() {
        return webhookRepository.findAll().stream()
                .map(x -> {
                    try {
                        return getWebhookEventDto(x);
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    public List<WebhookEventDto> getAllWebhooks(Long domainId) {
        return webhookRepository.findByDomain_Id(domainId).stream()
                .map(x -> {
                    try {
                        return getWebhookEventDto(x);
                    } catch (GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
    }

    public WebhookEventDto getById(Long id) throws GeneralSecurityException {
        WebhookEvent event = webhookRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(String.format("WebhookEventType with id: %d cannot be found", id)));
        return getWebhookEventDto(event);
    }

    public WebhookEventDto getById(Long domainId, Long id) throws GeneralSecurityException {
        WebhookEvent event = webhookRepository.findByIdAndDomain_Id(id, domainId)
                .orElseThrow(() -> new MissingElementException(String.format("WebhookEventType with id: %d cannot be found", id)));
        return getWebhookEventDto(event);
    }

    private WebhookEventDto getWebhookEventDto(WebhookEvent event) throws GeneralSecurityException {
        WebhookEventDto dto = modelMapper.map(event, WebhookEventDto.class);
        dto.setTokenValue(event.getTokenValue() == null ? null : encryptionService.decrypt(event.getTokenValue()));
        return dto;
    }

    private boolean checkPrivileges(Long id, String username, Domain domain) {
        return userService.isAdmin(username) || userService.isUserAdminInAnyDomain(domain != null ? List.of(domain) : List.of(), username);
    }

}
