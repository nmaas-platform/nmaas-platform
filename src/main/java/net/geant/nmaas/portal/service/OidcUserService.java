package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.auth.OidcApprovals;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OidcUserService {

    User checkUser(OidcUser oidcUser);

    User registerNewUser(OidcUser oidcUser);

    User registerNewUser(OidcApprovals oidcUser);

    boolean externalUserRequiresLinking(OidcUser oidcUser);

    boolean externalUserRequiresAupAndPn(OidcUser oidcUser);

    User linkUser(String email, String samlToken, String firstName, String lastName);

}
