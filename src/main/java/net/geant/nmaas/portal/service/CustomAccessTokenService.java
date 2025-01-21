package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserApiTokens;

import java.util.List;

public interface CustomAccessTokenService {

    void invalidate(Long id);
    UserApiTokens createToken(User user, String name);
    List<UserApiTokens> getAll(Long userId);

    void delete(Long id);

}
