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

    @Value("${notification.path}")
    private String path;

    @Override
    public void sendEmail(EmailConfirmation emailConfirmation, String token) {
        final String uri = String.format("%s:%s%s", url, port, path);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", token);
        HttpEntity<EmailConfirmation> entity = new HttpEntity<EmailConfirmation>(emailConfirmation, headers);

        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }
}
