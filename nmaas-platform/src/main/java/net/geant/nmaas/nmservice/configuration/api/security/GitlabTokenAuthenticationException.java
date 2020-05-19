package net.geant.nmaas.nmservice.configuration.api.security;

import net.geant.nmaas.portal.api.exception.AuthenticationException;

public class GitlabTokenAuthenticationException extends AuthenticationException {
    public GitlabTokenAuthenticationException(String message) {
        super(message);
    }
}
