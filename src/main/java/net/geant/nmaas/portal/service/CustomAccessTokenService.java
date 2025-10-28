package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.domain.UserApiTokenView;
import net.geant.nmaas.portal.persistence.entity.User;

import java.util.List;

public interface CustomAccessTokenService {

    void invalidate(Long id);
    UserApiTokenView createToken(User user, String name);
    List<UserApiTokenView> getAll(Long userId);

    void delete(Long id);

}
