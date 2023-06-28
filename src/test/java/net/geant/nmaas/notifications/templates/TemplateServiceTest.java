package net.geant.nmaas.notifications.templates;

import net.geant.nmaas.notifications.MailTemplateElements;
import net.geant.nmaas.notifications.templates.entities.MailTemplate;
import net.geant.nmaas.notifications.templates.repository.MailTemplateRepository;
import net.geant.nmaas.portal.service.impl.LocalFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TemplateServiceTest {

    private MailTemplateRepository repository = mock(MailTemplateRepository.class);
    private LocalFileStorageService fileStorageService = mock(LocalFileStorageService.class);

    private TemplateService templateService;

    @BeforeEach
    void setup() {
        this.templateService = new TemplateService(repository, fileStorageService, new ModelMapper());
    }

    @Test
    void shouldThrowExceptionOnMissingHtmlTemplate() {
        assertThrows(IllegalArgumentException.class, () -> {
            templateService.getHTMLTemplate();
        });
    }

    @Test
    void shouldReturnAllMailTemplates() {
        when(repository.findAll()).thenReturn(Arrays.asList(
                new MailTemplate(1L, MailType.ACCOUNT_ACTIVATED, null, null),
                new MailTemplate(2L, MailType.ACCOUNT_BLOCKED, null, null)
        ));
        assertEquals(2, templateService.getMailTemplates().size());
    }

    @Test
    void shouldReturnMailTemplateOfGivenType() {
        when(repository.findByMailType(MailType.ACCOUNT_ACTIVATED))
                .thenReturn(Optional.of(new MailTemplate(1L, MailType.ACCOUNT_ACTIVATED, null, null)));
        assertNotNull(templateService.getMailTemplate(MailType.ACCOUNT_ACTIVATED));
        assertThrows(IllegalArgumentException.class, () -> {
            templateService.getMailTemplate(MailType.ACCOUNT_BLOCKED);
        });
    }

    @Test
    void shouldHandleHtmlTemplateStorage() {
        assertThrows(IllegalArgumentException.class, () -> {
            templateService.storeHTMLTemplate(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            templateService.storeHTMLTemplate(new MockMultipartFile("name", "content".getBytes()));
        });
        assertDoesNotThrow(() -> {
            templateService.storeHTMLTemplate(new MockMultipartFile("name", "name", MailTemplateElements.HTML_TYPE, "<html></html>".getBytes()));
        });
    }

    @Test
    void shouldUpdateHtmlTemplate() {
        assertThrows(IllegalArgumentException.class, () -> {
            templateService.updateHTMLTemplate(null);
        });
    }

}
