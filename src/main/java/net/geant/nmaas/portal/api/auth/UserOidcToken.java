package net.geant.nmaas.portal.api.auth;


public record UserOidcToken(String token, String refreshToken, String oidcToken) {
}
