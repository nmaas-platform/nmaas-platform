package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.AccessToken;
import net.geant.nmaas.portal.persistent.repositories.AccessTokenRepository;
import net.geant.nmaas.portal.service.CustomAccessTokenService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomAccessTokenServiceImpl implements CustomAccessTokenService {

    private final AccessTokenRepository accessTokenRepository;

    @Override
    public void invalidate(Long id) {
        AccessToken token = findToken(id);
        token.setValid(false);
        accessTokenRepository.save(token);
    }

    @Override
    public AccessToken createToken(Long userId, String name) {
        AccessToken token = createNewToken(userId, name);
        return accessTokenRepository.save(token);
    }

    @Override
    public List<AccessToken> getAll(Long userId) {
        return accessTokenRepository.findAllByUserId(userId);
    }

    private AccessToken createNewToken(Long userId, String name) {
        AccessToken token = new AccessToken();
        token.setName(name);
        token.setUserId(userId);
        token.setTokenValue(generateToken());
        token.setValid(true);
        return token;
        }

    private String generateToken() {
        // uuid is a placeholder for now
        return UUID.randomUUID().toString();
    }

    private AccessToken findToken(Long id) {
        return accessTokenRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Could not find access token with id: " + id));
    }
}
