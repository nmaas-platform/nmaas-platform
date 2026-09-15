package net.geant.nmaas.portal.api.webhooks;

public class WebhookTemplateProcessingException extends RuntimeException {

    public WebhookTemplateProcessingException(String message) {
        super(message);
    }

    public WebhookTemplateProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebhookTemplateProcessingException(Throwable cause) {
        super(cause);
    }
}
