package net.geant.nmaas.orchestration.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.exceptions.WebServiceCommunicationException;
import net.geant.nmaas.portal.api.domain.WebhookEventDto;
import net.geant.nmaas.portal.service.WebhookEventService;
import org.modelmapper.ModelMapper;
import org.quartz.Job;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
@Slf4j
public abstract class WebhookJob implements Job {

    protected final RestClient restClient;
    protected final WebhookEventService webhookEventService;
    protected final ModelMapper modelMapper;

    protected void callWebhook(WebhookEventDto webhook, Object payload) {
        RestClient.RequestBodySpec request = restClient.post()
                .uri(webhook.getTargetUrl())
                .body(payload);


        if ("Authorization".equals(webhook.getAuthorizationHeader())) {
            request.header("Authorization", "Bearer " + webhook.getTokenValue());
        } else if (webhook.getAuthorizationHeader() != null) {
            request.header(webhook.getAuthorizationHeader(), webhook.getTokenValue());
        }

        //throw WebServiceCommunicationException for any possible error in calling webhook
        try {
            ResponseEntity<String> response = request.retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(),
                            (req, res) -> {
                                String errorMessage = "Webhook call failed with status: " + res.getStatusCode() + ", body: " + res.getBody();
                                log.error(errorMessage);
                                throw new WebServiceCommunicationException(errorMessage);
                            }
                    )
                    .toEntity(String.class);

            log.info("Webhook call successful. Response: {}", response.getBody());
        } catch (WebServiceCommunicationException e) {
            throw e;
        } catch (Exception error) {
            log.error("Webhook call failed with error: {}", error.getMessage(), error);
            throw new WebServiceCommunicationException("Webhook call failed: " + error.getMessage());
        }
    }
}
