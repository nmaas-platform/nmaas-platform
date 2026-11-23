package net.geant.nmaas.utils.captcha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaptchaValidatorTest {

    private RestTemplate restTemplate;

    private CaptchaValidator validator;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        validator = new CaptchaValidator(restTemplate);
    }

    @Test
    void shouldSkipValidationWhenSecretIsNotProvided() {
        ReflectionTestUtils.setField(validator, "secret", "not_provided");

        assertTrue(validator.verifyToken("token"));

        verifyNoGoogleRequest();
    }

    @Test
    void shouldSkipValidationWhenSecretIsEmpty() {
        ReflectionTestUtils.setField(validator, "secret", "");

        assertTrue(validator.verifyToken("token"));

        verifyNoGoogleRequest();
    }

    @Test
    void shouldSkipValidationWhenSecretIsNull() {
        ReflectionTestUtils.setField(validator, "secret", null);

        assertTrue(validator.verifyToken("token"));

        verifyNoGoogleRequest();
    }

    @Test
    void shouldSkipValidationWhenSecretIsNotProvidedInDifferentCase() {
        ReflectionTestUtils.setField(validator, "secret", "NOT_PROVIDED");

        assertTrue(validator.verifyToken("token"));

        verifyNoGoogleRequest();
    }

    @Test
    void shouldReturnTrueWhenGoogleRespondsWithSuccess() {
        ReflectionTestUtils.setField(validator, "secret", "secret-key");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("success", true));

        assertTrue(validator.verifyToken("token"));
    }

    @Test
    void shouldReturnFalseWhenGoogleRespondsWithFailure() {
        ReflectionTestUtils.setField(validator, "secret", "secret-key");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(Map.of("success", false));

        assertFalse(validator.verifyToken("token"));
    }

    @Test
    void shouldLogWarningOnInitializationWhenCaptchaDisabled() {
        ReflectionTestUtils.setField(validator, "secret", "not_provided");

        assertDoesNotThrow(validator::warnIfCaptchaDisabled);
    }

    @Test
    void shouldNotThrowOnInitializationWhenCaptchaEnabled() {
        ReflectionTestUtils.setField(validator, "secret", "secret-key");

        assertDoesNotThrow(validator::warnIfCaptchaDisabled);
    }

    private void verifyNoGoogleRequest() {
        verify(restTemplate, never()).postForObject(anyString(), any(), eq(Map.class));
    }

}
