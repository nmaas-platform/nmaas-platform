package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserApiTokens;
import net.geant.nmaas.portal.persistent.repositories.UserApiTokensRepository;
import net.geant.nmaas.portal.service.CustomAccessTokenService;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomAccessTokenServiceImpl implements CustomAccessTokenService {

    private final UserApiTokensRepository userApiTokensRepository;

    @Override
    public void invalidate(Long id) {
        UserApiTokens token = findToken(id);
        token.setValid(false);
        userApiTokensRepository.save(token);
    }

    @Override
    public void delete(Long id) {
        UserApiTokens token = findToken(id);
        if(!token.isValid()) {
            token.setDeleted(true);
            userApiTokensRepository.save(token);
        }else {
            throw new IllegalArgumentException("Token is still valid, can not delete valid token");
        }

    }

    @Override
    public UserApiTokens createToken(User user, String name) {
        UserApiTokens token = createNewToken(user, name);
        return userApiTokensRepository.save(token);
    }

    @Override
    public List<UserApiTokens> getAll(Long userId) {
        return userApiTokensRepository.findAllByUserId(userId);
    }

    private UserApiTokens createNewToken(User user, String name) {
        UserApiTokens token = new UserApiTokens();
        token.setName(name);
        token.setUser(user);
        token.setTokenValue(generateToken());
        token.setValid(true);
        return token;
        }

    private String generateToken() {
        // uuid is a placeholder for now
        return UUID.randomUUID().toString();
    }

    private UserApiTokens findToken(Long id) {
        return userApiTokensRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Could not find access token with id: " + id));
    }
}
