package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.security.EncryptionService;
import net.geant.nmaas.portal.api.webhooks.WebhookTemplateController;
import net.geant.nmaas.portal.domain.WebhookEventDto;
import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import net.geant.nmaas.portal.persistence.repositories.WebhookEventRepository;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.modelmapper.ModelMapper;

import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebhookEventServiceTest {

    private final WebhookEventRepository webhookEventRepository = mock(WebhookEventRepository.class);
    private final EncryptionService encryptionService = mock(EncryptionService.class);
    private final UserService userService = mock(UserService.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final AutoWebhookTemplateService autoWebhookTemplateService =  new AutoWebhookTemplateService();

    private final WebhookEventService webhookEventService = new WebhookEventService(webhookEventRepository, encryptionService, modelMapper, userService, autoWebhookTemplateService);

    private WebhookEventDto webhookEventDto;
    private WebhookEvent webhookEvent;

    @BeforeEach
    void setUp() throws GeneralSecurityException {
        autoWebhookTemplateService.init();
        webhookEventDto = new WebhookEventDto(1L, "webhook", "https://example.com/webhook", WebhookEventType.APPLICATION_DEPLOYMENT);
        webhookEvent = new WebhookEvent(1L, "webhook", "https://example.com/webhook", WebhookEventType.APPLICATION_DEPLOYMENT);
        webhookEventService.create(webhookEventDto);
    }

    @Test
    void shouldPerformCrudWebhookEventActions() throws GeneralSecurityException {
        webhookEventDto = new WebhookEventDto(2L, "webhook2", "https://example.com/webhook2", WebhookEventType.DOMAIN_ACTION, "xxxxyyyy", "Authorization", null, null);
        webhookEvent = new WebhookEvent(2L, "webhook2", "https://example.com/webhook2", WebhookEventType.DOMAIN_ACTION, "sjxV/ytRIoHjXy+CtXMzD4T+bntbqzQX25eztXbJ9r4gIZXT", "Authorization", null, null);
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
        when(userService.isAdmin("test")).thenReturn(true);
        webhookEventService.update(webhookEventDto);

        when(webhookEventRepository.existsById(2L)).thenReturn(true);
        doNothing().when(webhookEventRepository).deleteById(2L);

        when(userService.isAdmin("test")).thenReturn(true);
        webhookEventService.remove(2L);
    }

    @Test
    void failedDueToFalseTemplate() throws GeneralSecurityException {
        webhookEventDto = new WebhookEventDto(2L, "webhook2", "https://example.com/webhook2", WebhookEventType.DOMAIN_ACTION, null, null, null, "{\"domain\": \"id\": $DOMAINVIEW_ID, \"name22\": $DOMAINVIEW_NAME, \"codename\": $DOMAINVIEW_CODENAME, \"active\": $DOMAINVIEW_ACTIVE}, \"action\": $ACTION, \"type\": $WEBHOOKEVENTTYPE, \"action22\": \"test\"}");
       // webhookEvent = new WebhookEvent(2L, "webhook2", "https://example.com/webhook2", WebhookEventType.DOMAIN_ACTION, null, null, null, null);
        when(webhookEventRepository.save(isA(WebhookEvent.class))).thenReturn(webhookEvent);
        assertThrows(IllegalArgumentException.class, () -> {
            webhookEventService.create(webhookEventDto);
        });
    }

    @Test
    void failedDueToFalseVariable() throws GeneralSecurityException {
        webhookEventDto = new WebhookEventDto(2L, "webhook2", "https://example.com/webhook2", WebhookEventType.DOMAIN_ACTION, null, null, null, "{\"domain\": {\"id\": $DOMAINVIEW_ID, \"name22\": $DOMAINVIEW_DESCR, \"codename\": $DOMAINVIEW_CODENAME, \"active\": $DOMAINVIEW_ACTIVE}, \"action\": $ACTION, \"type\": $WEBHOOKEVENTTYPE, \"action22\": \"test\"}");
        // webhookEvent = new WebhookEvent(2L, "webhook2", "https://example.com/webhook2", WebhookEventType.DOMAIN_ACTION, null, null, null, null);
        when(webhookEventRepository.save(isA(WebhookEvent.class))).thenReturn(webhookEvent);
        assertThrows(IllegalArgumentException.class, () -> {
            webhookEventService.create(webhookEventDto);
        });
    }

    @Test
    void shouldGetAllWebhookEvents() throws GeneralSecurityException {
        // when(encryptionService.decrypt(anyString())).thenAnswer(i -> "xxxxyyyy");
        when(webhookEventRepository.findAll()).thenReturn(Collections.singletonList(webhookEvent));

        List<WebhookEventDto> webhooks = webhookEventService.getAllWebhooks();

        assertNotNull(webhooks);
        assertEquals(1, webhooks.size());
        WebhookEventDto created = webhooks.getFirst();
        assertEquals(webhookEventDto.getId(), created.getId());
        assertEquals(webhookEventDto.getName(), created.getName());
        assertEquals(webhookEventDto.getTargetUrl(), created.getTargetUrl());
        assertEquals(webhookEventDto.getEventType(), created.getEventType());
    }

    @Test
    void shouldThrowExceptionWhenWebhookNotFound() throws GeneralSecurityException {
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
            when(userService.isAdmin("test")).thenReturn(true);
            webhookEventService.remove(999L);
        });
    }

    @Test
    void variablesAndDefaultTemplateForUserAssignment() {
        WebhookEventType eventType = WebhookEventType.APPLICATION_DEPLOYMENT;
        Set<String> available = autoWebhookTemplateService.getAvailableVariables(eventType);
        assertFalse(available.isEmpty(), "Expected variables for " + eventType);

        String template = autoWebhookTemplateService.getDefaultTemplate(eventType);
        assertNotNull(template);

        available.forEach(var -> {
            assertTrue(Stream.of("$APPDEPLOYMENT_DEPLOYMENTID","$APPDEPLOYMENT_DEPLOYMENTNAME","$APPDEPLOYMENT_DOMAIN","$APPDEPLOYMENT_STATE","$APPDEPLOYMENT_OWNER", "$APPDEPLOYMENT_APPNAME", "$WEBHOOKEVENTTYPE", "$APPDATA_KEY", "$LOGICAL_DATE").toList().contains(var), () -> "Missing variable " + var + " for " + eventType);
            assertTrue(template.contains(var), () -> "Variable " + var + " is missing from the template of " + eventType);
        });
    }

    @Test
    void variablesAndDefaultTemplateForDomainAction() {
        WebhookEventType eventType = WebhookEventType.DOMAIN_ACTION;
        Set<String> available = autoWebhookTemplateService.getAvailableVariables(eventType);
        assertFalse(available.isEmpty(), "Expected variables for " + eventType);

        String template = autoWebhookTemplateService.getDefaultTemplate(eventType);
        assertNotNull(template);

        available.forEach(var -> {
            assertTrue(Stream.of("$DOMAINVIEW_ID", "$DOMAINVIEW_NAME", "$DOMAINVIEW_CODENAME", "$DOMAINVIEW_ACTIVE", "$DOMAINVIEW_DELETED", "$WEBHOOKEVENTTYPE", "$ACTION").toList().contains(var), () -> "Missing variable " + var + " for " + eventType);
            assertTrue(template.contains(var), () -> "Variable " + var + " is missing from the template of " + eventType);
        });
    }

    @Test
    void variablesAndDefaultTemplateForDomainGroupAction() {
        WebhookEventType eventType = WebhookEventType.DOMAIN_GROUP_ACTION;
        Set<String> available = autoWebhookTemplateService.getAvailableVariables(eventType);
        assertFalse(available.isEmpty(), "Expected variables for " + eventType);

        String template = autoWebhookTemplateService.getDefaultTemplate(eventType);
        assertNotNull(template);

        available.forEach(var -> {
            assertTrue(Stream.of("$DOMAINGROUP_ID", "$DOMAINGROUP_NAME", "$DOMAINGROUP_CODENAME", "$DOMAINGROUP_MANAGERS", "$WEBHOOKEVENTTYPE", "$ACTION").toList().contains(var), () -> "Missing variable " + var + " for " + eventType);
            assertTrue(template.contains(var), () -> "Variable " + var + " is missing from the template of " + eventType);
        });
    }

    @Test
    void variablesAndDefaultTemplateForApplicationDeployment() {
        WebhookEventType eventType = WebhookEventType.USER_ASSIGNMENT;
        Set<String> available = autoWebhookTemplateService.getAvailableVariables(eventType);
        assertFalse(available.isEmpty(), "Expected variables for " + eventType);

        String template = autoWebhookTemplateService.getDefaultTemplate(eventType);
        assertNotNull(template);

        available.forEach(var -> {
            assertTrue(Stream.of("$USER", "$DOMAIN_ID", "$DOMAIN_NAME", "$DOMAIN_CODENAME", "$DOMAIN_ACTIVE", "$DOMAIN_DELETED", "$ROLE","$WEBHOOKEVENTTYPE", "$ACTION").toList().contains(var), () -> "Missing variable " + var + " for " + eventType);
            assertTrue(template.contains(var), () -> "Variable " + var + " is missing from the template of " + eventType);
        });
    }
}