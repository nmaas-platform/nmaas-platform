package net.geant.nmaas.gitlab.exceptions;

public class GitLabNotFoundException extends RuntimeException {
    public GitLabNotFoundException(String message){
        super(message);
    }
}
