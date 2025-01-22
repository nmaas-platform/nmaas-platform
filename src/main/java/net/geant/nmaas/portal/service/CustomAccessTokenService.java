package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.user.UserApiTokenView;
import net.geant.nmaas.portal.persistent.entity.User;

import java.util.List;

public interface CustomAccessTokenService {

    void invalidate(Long id);
    UserApiTokenView createToken(User user, String name);
    List<UserApiTokenView> getAll(Long userId);

    void delete(Long id);

}
