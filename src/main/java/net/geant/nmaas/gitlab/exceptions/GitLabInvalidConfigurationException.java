package net.geant.nmaas.gitlab.exceptions;

public class GitLabInvalidConfigurationException extends RuntimeException {
    public GitLabInvalidConfigurationException(String message){
        super(message);
    }
}
