package net.geant.nmaas.nmservice.configuration.api.security;

import org.springframework.security.core.AuthenticationException;

public class GitlabTokenAuthenticationException extends AuthenticationException {
    public GitlabTokenAuthenticationException(String message) {
        super(message);
    }
}
