package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.model.EmailConfirmation;
import net.geant.nmaas.portal.auth.basic.TokenAuthenticationService;
import net.geant.nmaas.portal.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Value("${notification.url}")
    private String url;

    @Value("${notification.port}")
    private String port;

    @Value("${notification.path.withToken}")
    private String pathWithToken;

    @Autowired
    private TokenAuthenticationService tokenAuthenticationService;

    @Override
    public void sendEmail(EmailConfirmation emailConfirmation) {
        final String uri = String.format("%s:%s%s", url, port, pathWithToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + tokenAuthenticationService.getAnonymousAccessToken());
        HttpEntity<EmailConfirmation> entity = new HttpEntity<>(emailConfirmation, headers);

        restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
    }
}