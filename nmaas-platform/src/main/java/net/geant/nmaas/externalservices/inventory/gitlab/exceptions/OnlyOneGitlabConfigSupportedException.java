package net.geant.nmaas.externalservices.inventory.gitlab.exceptions;

public class OnlyOneGitlabConfigSupportedException extends Exception {
    public OnlyOneGitlabConfigSupportedException(String message){
        super(message);
    }
}
