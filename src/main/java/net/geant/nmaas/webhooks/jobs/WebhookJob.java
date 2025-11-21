package net.geant.nmaas.webhooks.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.exceptions.WebServiceCommunicationException;
import net.geant.nmaas.portal.domain.WebhookEventDto;
import net.geant.nmaas.portal.service.WebhookHistoryService;
import net.geant.nmaas.portal.service.impl.WebhookEventService;
import org.modelmapper.ModelMapper;
import org.quartz.Job;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Slf4j
public abstract class WebhookJob implements Job {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    protected final RestClient restClient;
    protected final WebhookEventService webhookEventService;
    protected final ModelMapper modelMapper;
    protected final WebhookHistoryService webhookHistoryService;

    protected void callWebhook(WebhookEventDto webhook, Object payload) {
        RestClient.RequestBodySpec request = restClient.post()
                .uri(webhook.getTargetUrl())
                .body(payload);

        if (AUTHORIZATION_HEADER.equals(webhook.getAuthorizationHeader())) {
            request.header("Authorization", "Bearer " + webhook.getTokenValue());
        } else if (webhook.getAuthorizationHeader() != null) {
            request.header(webhook.getAuthorizationHeader(), webhook.getTokenValue());
        }

        // throw WebServiceCommunicationException for any possible error in calling webhook
        try {
            ResponseEntity<String> response = request
                    .retrieve()
                    .toEntity(String.class);

            String body = response.getBody();
            webhookHistoryService.create(webhook, payload, response.getStatusCode().value(), body);

            if (!response.getStatusCode().is2xxSuccessful()) {
                String errorMessage = "Webhook call failed with status: " + response.getStatusCode().value() + ", body: " + body;
                log.error(errorMessage);
                throw new WebServiceCommunicationException(errorMessage);
            }
            log.info("Webhook call for {} was successful. Response: {}", webhook.getEventType(), body);
        } catch (WebServiceCommunicationException e) {
            throw e;
        } catch (Exception error) {
            log.error("Webhook call failed: {}", error.getMessage(), error);
            webhookHistoryService.create(webhook, payload, null, null);
            throw new WebServiceCommunicationException("Webhook call failed: " + error.getMessage());
        }

    }

}
