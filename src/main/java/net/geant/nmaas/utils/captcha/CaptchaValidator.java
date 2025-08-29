package net.geant.nmaas.utils.captcha;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class CaptchaValidator {

    // set default value to `not_provided`
    @Value("${captcha.secret:not_provided}")
    private String secret;

    private static final String GOOGLE_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    private final RestTemplate restTemplate;

    @Autowired
    public CaptchaValidator() {
        restTemplate = new RestTemplate();
    }

    public boolean verifyToken(String token) {
        if (secret.equalsIgnoreCase("not_provided") || secret.isEmpty()) {
            log.info("Skipped captcha validation due to not provided token");
            return true; // validate if secret is not provided
        }
        final Map response = restTemplate.postForObject(prepareUrl(token), null, Map.class);
        return (boolean) Objects.requireNonNull(response).get("success");
    }

    private String prepareUrl(String token) {
        return String.format(GOOGLE_URL, secret, token);
    }

}
