package net.geant.nmaas.portal.api.auth;


public record UserOidcToken(String token, String refreshToken, String oidcToken) {

    /**
     * Masked on purpose: none of the tokens must ever reach logs.
     */
    @Override
    public String toString() {
        return "UserOidcToken[token=***, refreshToken=***, oidcToken=***]";
    }
}
