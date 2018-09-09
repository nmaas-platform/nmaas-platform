package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.model.EmailConfirmation;
import net.geant.nmaas.portal.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Value("${notification.url}")
    private String url;

    @Value("${notification.port}")
    private String port;

    @Value("${notification.path.withToken}")
    private String pathWithToken;

    @Value("${notification.path.withoutToken}")
    private String pathWithoutToken;

    @Override
    public void sendEmailWithToken(EmailConfirmation emailConfirmation, String token) {
        final String uri = String.format("%s:%s%s", url, port, pathWithToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", token);
        HttpEntity<EmailConfirmation> entity = new HttpEntity<EmailConfirmation>(emailConfirmation, headers);

        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }

    @Override
    public void sendEmailWithoutToken(EmailConfirmation emailConfirmation) {
        final String uri = String.format("%s:%s%s", url, port, pathWithoutToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        HttpEntity<EmailConfirmation> entity = new HttpEntity<EmailConfirmation>(emailConfirmation);

        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }
}
