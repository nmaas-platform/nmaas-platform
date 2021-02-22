package net.geant.nmaas.utils.captcha;

import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CaptchaValidator {

    @Value("${captcha.secret}")
    private String secret;

    private static final String GOOGLE_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    private final RestTemplate restTemplate;

    @Autowired
    public CaptchaValidator(){
        restTemplate = new RestTemplate();
    }

    public boolean verifyToken(String token) {
        final Map response = restTemplate.postForObject(prepareUrl(token), null, Map.class);
        return (boolean) Objects.requireNonNull(response).get("success");
    }

    private String prepareUrl(String token) {
        return String.format(GOOGLE_URL, secret, token);
    }
}
