package net.geant.nmaas.orchestration.exceptions;

import lombok.Getter;

@Getter
public class WebServiceCommunicationException extends RuntimeException {

    private int responseStatus;
    private String responseBody;

    public WebServiceCommunicationException(String message) {
        super(message);
    }

    public WebServiceCommunicationException(String message, int responseStatus, String responseBody) {
        super(message);
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
    }

}
