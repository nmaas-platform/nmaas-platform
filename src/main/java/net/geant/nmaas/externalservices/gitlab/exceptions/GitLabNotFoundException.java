package net.geant.nmaas.externalservices.gitlab.exceptions;

public class GitLabNotFoundException extends RuntimeException {
    public GitLabNotFoundException(String message){
        super(message);
    }
}
