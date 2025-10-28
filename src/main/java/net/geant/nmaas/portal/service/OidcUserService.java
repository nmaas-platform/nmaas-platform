package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OidcUserService {

    User checkUser(OidcUser oidcUser);

    User register(OidcUser user, Domain globalDomain);

    User registerNewUser(OidcUser oidcUser);

    boolean externalUserRequiresLinking(OidcUser oidcUser);

    User linkUser(String email, String samlToken, String firstName, String lastName);

}
