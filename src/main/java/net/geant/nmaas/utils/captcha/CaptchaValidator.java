package net.geant.nmaas.utils.captcha;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class CaptchaValidator {

    private static final String NOT_PROVIDED = "not_provided";

    // set default value to `not_provided`
    @Value("${captcha.secret:not_provided}")
    private String secret;

    private static final String GOOGLE_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    private final RestClient restClient;

    @Autowired
    public CaptchaValidator() {
        restClient = RestClient.create();
    }

    CaptchaValidator(RestClient restClient) {
        this.restClient = restClient;
    }

    @PostConstruct
    void warnIfCaptchaDisabled() {
        if (isDisabled()) {
            log.warn("Captcha validation is DISABLED (captcha.secret not configured). "
                    + "Registration, password reset and contact form endpoints are not protected by captcha. "
                    + "Set the CAPTCHA_SECRET environment variable to enable it.");
        }
    }

    public boolean verifyToken(String token) {
        if (isDisabled()) {
            log.info("Skipped captcha validation due to not provided token");
            return true; // validate if secret is not provided
        }
        final Map response = restClient.post()
                .uri(URI.create(prepareUrl(token)))
                .retrieve()
                .body(Map.class);
        return (boolean) Objects.requireNonNull(response).get("success");
    }

    private boolean isDisabled() {
        return secret == null || secret.isBlank() || secret.equalsIgnoreCase(NOT_PROVIDED);
    }

    private String prepareUrl(String token) {
        return String.format(GOOGLE_URL, secret, token);
    }

}
