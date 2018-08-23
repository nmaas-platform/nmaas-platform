package net.geant.nmaas.externalservices.inventory.gitlab.exceptions;

public class GitLabConfigNotFoundException extends RuntimeException {
    public GitLabConfigNotFoundException(String message){
        super(message);
    }
}
