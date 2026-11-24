package net.geant.nmaas.portal.api.auth;

/**
 * Request payload of the account linking endpoint.
 *
 * <p>Identity of the external user is resolved exclusively from the OIDC ID token
 * validated server-side. The local account is resolved by the email claim of that
 * token, so the email provided here has to match it, and the password is used to
 * prove ownership of the local account.</p>
 */
public record OidcLogin(String email,
                        String password,
                        String oidcToken) {

    /**
     * Masked on purpose: the password and the ID token must never reach logs.
     */
    @Override
    public String toString() {
        return "OidcLogin[email=" + email + ", password=***, oidcToken=***]";
    }
}
