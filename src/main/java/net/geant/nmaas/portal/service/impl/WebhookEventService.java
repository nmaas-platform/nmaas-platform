package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.domain.WebhookEventDto;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.security.EncryptionService;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.persistence.repositories.WebhookEventRepository;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WebhookEventService {

    private static final String WEBHOOK_EVENT_NOT_FOUND = "WebhookEvent not found.";

    private final WebhookEventRepository webhookRepository;
    private final EncryptionService encryptionService;
    private final ModelMapper modelMapper;
    private final UserService userService;

    public WebhookEvent create(WebhookEventDto webhookEventDto) throws GeneralSecurityException {
        WebhookEvent webhookEvent = new WebhookEvent();
        setWebhookEvent(webhookEvent, webhookEventDto);
        return webhookRepository.save(webhookEvent);
    }

    public WebhookEventDto update(WebhookEventDto webhookEventDto, String username) throws GeneralSecurityException {
        WebhookEvent webhookEvent = webhookRepository.findById(webhookEventDto.getId())
                .orElseThrow(() -> new MissingElementException(WEBHOOK_EVENT_NOT_FOUND));
      //  for update domain admin can not change domain of webhook event
        if (userService.isAdmin(username) || (userService.isUserAdminInAnyDomain(webhookEvent.getDomain() != null ? List.of(webhookEvent.getDomain()) : List.of(), username)
                && webhookEventDto.getDomain() != null && webhookEvent.getDomain().getId().equals(webhookEventDto.getDomain().getId()))) {
            setWebhookEvent(webhookEvent, webhookEventDto);
            webhookEvent = webhookRepository.save(webhookEvent);
            WebhookEventDto dto = modelMapper.map(webhookEvent, WebhookEventDto.class);
            dto.setTokenValue(webhookEvent.getTokenValue() == null ? null : encryptionService.decrypt(webhookEvent.getTokenValue()));
            return dto;
        } else {
            throw new IllegalArgumentException("No access to webhook " + webhookEvent.getId());
        }
    }

    private void setWebhookEvent(WebhookEvent webhookEvent, WebhookEventDto webhookEventDto) throws GeneralSecurityException {
        //domain is combined only with APPLICATION_DEPLOYMENT, APPLICATION_REMOVAL and USER_ASSIGNMENT
        if (webhookEventDto.getDomain() != null && Stream.of(WebhookEventType.DOMAIN_ACTION, WebhookEventType.DOMAIN_GROUP_ACTION).anyMatch(x -> webhookEventDto.getEventType().equals(x)))
            throw new IllegalArgumentException("Domain can not combine with " + webhookEventDto.getEventType());

        webhookEvent.setName(webhookEventDto.getName());
        webhookEvent.setTargetUrl(webhookEventDto.getTargetUrl());
        webhookEvent.setEventType(webhookEventDto.getEventType());
        webhookEvent.setTokenValue(webhookEventDto.getTokenValue() == null ? null : encryptionService.encrypt(webhookEventDto.getTokenValue()));
        webhookEvent.setAuthorizationHeader(webhookEventDto.getAuthorizationHeader());
        webhookEvent.setDomain(webhookEventDto.getDomain() != null ? new Domain(webhookEventDto.getDomain().getId()): null);
    }

    public void remove(Long id, String username) {
        WebhookEvent webhookEvent = webhookRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(WEBHOOK_EVENT_NOT_FOUND));
        if (checkPrivileges(webhookEvent.getId(), username, webhookEvent.getDomain())) {
            webhookRepository.delete(webhookEvent);
        } else {
            throw new IllegalArgumentException("No access to webhook " + webhookEvent.getId());
        }
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

    public WebhookEventDto getById(Long id, String username) throws GeneralSecurityException {
        WebhookEvent event = webhookRepository.findById(id)
                .orElseThrow(() -> new MissingElementException(String.format("WebhookEventType with id: %d cannot be found", id)));
        if (checkPrivileges(id, username, event.getDomain())) {
            WebhookEventDto dto = modelMapper.map(event, WebhookEventDto.class);
            dto.setTokenValue(event.getTokenValue() == null ? null : encryptionService.decrypt(event.getTokenValue()));
            return dto;
        } else {
            throw new IllegalArgumentException("No access to webhook " + id);
        }
    }

    private boolean checkPrivileges(Long id, String username, Domain domain) {
        return userService.isAdmin(username) || userService.isUserAdminInAnyDomain(domain != null ? List.of(domain) : List.of(), username);
    }
}
