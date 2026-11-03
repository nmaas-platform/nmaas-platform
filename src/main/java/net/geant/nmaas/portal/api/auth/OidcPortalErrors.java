package net.geant.nmaas.portal.api.auth;

/**
 * Error codes carried to the portal SPA in the {@code error} query parameter
 * of the {@code /login} route after a failed OIDC login attempt.
 *
 * <p>Single source of truth for every backend sender — the login flow in
 * {@link OIDCAuthController} and the OAuth2 filters configured in
 * {@code SecurityConfig} — and for the SPA, which maps each code to a
 * localized message on the login page. See the {@link OIDCAuthController}
 * class documentation for the full signal contract.</p>
 */
public final class OidcPortalErrors {

    /**
     * Name of the query parameter carrying the rejection or failure code.
     */
    public static final String ERROR_PARAMETER = "error";

    /**
     * Rejection code for an external identity that cannot be matched to a
     * usable local account: linked to a different account, incomplete claims,
     * or no matching account at all. The provider session is terminated
     * server-side before the user is sent back to the portal.
     */
    public static final String ACCOUNT_CONFLICT = "oidc-account-conflict";

    /**
     * Failure code for a broken OAuth2/OIDC dance itself: rejected
     * authorization at the provider, state mismatch, failed token exchange
     * or ID token validation. Emitted by the security filters, so the
     * provider session cannot be terminated server-side — the SPA should
     * invite the user to retry the sign-in.
     */
    public static final String LOGIN_FAILURE = "oidc-failure";

    private OidcPortalErrors() {
    }
}
