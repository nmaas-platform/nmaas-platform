package net.geant.nmaas.webhooks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class AppDeploymentWebhookDto {

    private AppDeploymentView appDeployment;
    private WebhookEventType webhookEventType;
    private Map<String, String> appData;

    public AppDeploymentWebhookDto(AppDeploymentView appDeployment, WebhookEventType webhookEventType) {
        this.appDeployment = appDeployment;
        this.webhookEventType = webhookEventType;
    }

    @Getter
    @Setter
    public static class AppDeploymentView {

        private String deploymentId;
        private String deploymentName;
        private String domain;
        private String state;
        private String owner;
        private String appName;

    }

}
