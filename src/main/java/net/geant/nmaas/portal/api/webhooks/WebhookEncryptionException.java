package net.geant.nmaas.portal.api.webhooks;

public class WebhookEncryptionException extends RuntimeException {

    public WebhookEncryptionException(String message) {
        super(message);
    }

    public WebhookEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebhookEncryptionException(Throwable cause) {
        super(cause);
    }
}
