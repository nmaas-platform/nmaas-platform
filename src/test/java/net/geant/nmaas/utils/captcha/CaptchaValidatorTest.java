package net.geant.nmaas.utils.captcha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaptchaValidatorTest {

    private RestClient restClient;

    private CaptchaValidator validator;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        validator = new CaptchaValidator(restClient);
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
        mockGoogleResponse(Map.of("success", true));

        assertTrue(validator.verifyToken("token"));
    }

    @Test
    void shouldReturnFalseWhenGoogleRespondsWithFailure() {
        ReflectionTestUtils.setField(validator, "secret", "secret-key");
        mockGoogleResponse(Map.of("success", false));

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

    private void mockGoogleResponse(Map<String, Object> body) {
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestSpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(restClient.post()).thenReturn(postSpec);
        when(postSpec.uri(any(URI.class))).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Map.class))).thenReturn(body);
    }

    private void verifyNoGoogleRequest() {
        verify(restClient, never()).post();
    }

}
