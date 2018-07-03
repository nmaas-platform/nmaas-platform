package net.geant.nmaas.externalservices.inventory.gitlab.exceptions;

public class GitLabConfigNotFoundException extends Exception {
    public GitLabConfigNotFoundException(String message){
        super(message);
    }
}
