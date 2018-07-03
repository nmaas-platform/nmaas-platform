package net.geant.nmaas.externalservices.inventory.gitlab.exceptions;

public class OnlyOneGitLabConfigSupportedException extends Exception {
    public OnlyOneGitLabConfigSupportedException(String message){
        super(message);
    }
}
