package net.geant.nmaas.portal.api.auth;

/**
 * Request payload of the first-login approvals endpoint for new OIDC users.
 *
 * <p>Identity of the external user (subject, email, names) is resolved exclusively
 * from the OIDC ID token validated server-side; only the requested username and
 * the approval flags are taken from this payload.</p>
 */
public record OidcApprovals(String oidcToken,
                            String username,
                            boolean isAupApprove,
                            boolean isPnApprove) {

    /**
     * Masked on purpose: the ID token must never reach logs.
     */
    @Override
    public String toString() {
        return "OidcApprovals[oidcToken=***, username=" + username
                + ", isAupApprove=" + isAupApprove
                + ", isPnApprove=" + isPnApprove + "]";
    }
}
