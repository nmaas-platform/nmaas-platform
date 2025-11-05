package net.geant.nmaas.webhooks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

@AllArgsConstructor
@Getter
@Setter
public class AppDeploymentWebhookDto {

    private AppDeploymentView appDeployment;
    private WebhookEventType webhookEventType;

    @Getter
    @Setter
    public static class AppDeploymentView {

        private String deploymentId;
        private String deploymentName;
        private String domain;
        private String state;
        private String owner;
        private String appName;
        @JsonProperty("logical_date")
        private String logicalDate;

    }

}
