package net.geant.nmaas.externalservices.inventory.gitlab.exceptions;

public class GitlabConfigNotFoundException extends Exception {
    public GitlabConfigNotFoundException(String message){
        super(message);
    }
}
