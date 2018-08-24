package net.geant.nmaas.externalservices.inventory.gitlab.exceptions;

public class OnlyOneGitLabConfigSupportedException extends RuntimeException {
    public OnlyOneGitLabConfigSupportedException(String message){
        super(message);
    }
}
