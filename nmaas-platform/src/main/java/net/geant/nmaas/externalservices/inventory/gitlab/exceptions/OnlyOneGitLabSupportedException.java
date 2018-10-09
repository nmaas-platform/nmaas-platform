package net.geant.nmaas.externalservices.inventory.gitlab.exceptions;

public class OnlyOneGitLabSupportedException extends RuntimeException {
    public OnlyOneGitLabSupportedException(String message){
        super(message);
    }
}
