package net.geant.nmaas.portal.api.webhooks;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebhookEventAdviceTest {

    private final WebhookEventAdvice advice = new WebhookEventAdvice();

    @Test
    void shouldTranslateFieldErrorsToBadRequestPayload() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        var bindingResult = mock(org.springframework.validation.BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("webhookEventDto", "name", "must not be blank"),
                new FieldError("webhookEventDto", "targetUrl", "must not be blank")
        ));

        ResponseEntity<Map<String, String>> result = advice.handleValidationExceptions(exception);

        assertEquals(400, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("must not be blank", result.getBody().get("name"));
        assertEquals("must not be blank", result.getBody().get("targetUrl"));
    }
}
