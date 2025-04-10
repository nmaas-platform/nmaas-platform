package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.domain.WebhookEventDto;
import net.geant.nmaas.portal.api.security.EncryptionService;
import net.geant.nmaas.portal.persistent.entity.WebhookEvent;
import net.geant.nmaas.portal.persistent.entity.WebhookEventType;
import net.geant.nmaas.portal.persistent.repositories.WebhookEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebhookEventServiceTest {

    WebhookEventRepository webhookEventRepository = mock(WebhookEventRepository.class);

    private final ModelMapper modelMapper = new ModelMapper();
    EncryptionService encryptionService = mock(EncryptionService.class);

    WebhookEventService webhookEventService = new WebhookEventService(webhookEventRepository, encryptionService, modelMapper);

    private WebhookEventDto webhookEventDto;
    private WebhookEvent webhookEvent;

    @BeforeEach
    void setUp() throws Exception {
        webhookEventDto = new WebhookEventDto(1L, "webhook", "https://example.com/webhook", WebhookEventType.APPLICATION_DEPLOYMENT);
        webhookEvent = new WebhookEvent(1L, "webhook", "https://example.com/webhook", WebhookEventType.APPLICATION_DEPLOYMENT);
        webhookEventService.create(webhookEventDto);
    }

    @Test
    void crudWebhookEvent() throws Exception {
        webhookEventDto = new WebhookEventDto(2L, "webhook2", "https://example.com/webhook2", WebhookEventType.DOMAIN_CREATION, "xxxxyyyy", "Authorization");
        webhookEvent = new WebhookEvent(2L, "webhook2", "https://example.com/webhook2", WebhookEventType.DOMAIN_CREATION, "sjxV/ytRIoHjXy+CtXMzD4T+bntbqzQX25eztXbJ9r4gIZXT", "Authorization");
        when(webhookEventRepository.save(isA(WebhookEvent.class))).thenReturn(webhookEvent);
        when(encryptionService.encrypt(anyString())).thenAnswer(i -> "sjxV/ytRIoHjXy+CtXMzD4T+bntbqzQX25eztXbJ9r4gIZXT");
        when(encryptionService.decrypt(anyString())).thenAnswer(i -> "xxxxyyyy");
        WebhookEvent created = webhookEventService.create(webhookEventDto);

        assertNotNull(created);
        assertEquals(webhookEventDto.getId(), created.getId());
        assertEquals(webhookEventDto.getName(), created.getName());
        assertEquals(webhookEventDto.getTargetUrl(), created.getTargetUrl());
        assertEquals(webhookEventDto.getEventType(), created.getEventType());
        assertEquals(webhookEventDto.getAuthorizationHeader(), created.getAuthorizationHeader());
        assertEquals(webhookEventDto.getTokenValue(), encryptionService.decrypt(created.getTokenValue()));

        webhookEventDto.setName("updated webhook");
        when(webhookEventRepository.findById(2L)).thenReturn(Optional.of(webhookEvent));
        webhookEventService.update(webhookEventDto);

        when(webhookEventRepository.existsById(2L)).thenReturn(true);
        doNothing().when(webhookEventRepository).deleteById(2L);

        webhookEventService.remove(2L);
    }

    @Test
    void shouldGetAllWebhookEvents() throws Exception {
        // when(encryptionService.decrypt(anyString())).thenAnswer(i -> "xxxxyyyy");
        when(webhookEventRepository.findAll()).thenReturn(Arrays.asList(webhookEvent));

        List<WebhookEventDto> webhooks = webhookEventService.getAllWebhooks();

        assertNotNull(webhooks);
        assertEquals(1, webhooks.size());
        WebhookEventDto created = webhooks.get(0);
        assertEquals(webhookEventDto.getId(), created.getId());
        assertEquals(webhookEventDto.getName(), created.getName());
        assertEquals(webhookEventDto.getTargetUrl(), created.getTargetUrl());
        assertEquals(webhookEventDto.getEventType(), created.getEventType());
    }

    @Test
    void shouldThrowExceptionWhenWebhookNotFound() throws Exception {
        // when(encryptionService.decrypt(anyString())).thenAnswer(i -> "xxxxyyyy");
        when(webhookEventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            webhookEventService.getById(999L);
        });
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentWebhook() {
        when(webhookEventRepository.existsById(999L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            webhookEventService.remove(999L);
        });
    }
}