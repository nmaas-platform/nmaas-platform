package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.users.UserApiTokenDto;
import net.geant.nmaas.portal.persistence.entity.User;

import java.util.List;

public interface CustomAccessTokenService {

    void invalidate(Long id);

    UserApiTokenDto createToken(User user, String name);

    List<UserApiTokenDto> getAll(Long userId);

    void delete(Long id);

}
