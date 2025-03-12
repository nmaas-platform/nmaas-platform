package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OidcUserService {

    User checkUser(OidcUser oidcUser);
    User register(OidcUser user, Domain globalDomain);
    User registerNewUser(OidcUser oidcUser);

}
