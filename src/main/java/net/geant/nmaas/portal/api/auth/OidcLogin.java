package net.geant.nmaas.portal.api.auth;

public record OidcLogin(String email,
                        String password,
                        String oidcToken,
                        String uuid,
                        String firstName,
                        String lastName) {
}
