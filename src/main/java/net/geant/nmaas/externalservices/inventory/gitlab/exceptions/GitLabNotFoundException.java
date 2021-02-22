package net.geant.nmaas.externalservices.inventory.gitlab.exceptions;

public class GitLabNotFoundException extends RuntimeException {
    public GitLabNotFoundException(String message){
        super(message);
    }
}
