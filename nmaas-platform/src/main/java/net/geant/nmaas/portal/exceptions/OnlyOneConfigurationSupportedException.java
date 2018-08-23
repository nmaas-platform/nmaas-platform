package net.geant.nmaas.portal.exceptions;

public class OnlyOneConfigurationSupportedException extends RuntimeException {
    public OnlyOneConfigurationSupportedException(String message){
        super(message);
    }
}
