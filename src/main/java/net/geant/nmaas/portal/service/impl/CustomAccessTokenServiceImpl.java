package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.users.UserApiTokenDto;
import net.geant.nmaas.portal.exceptions.DataConflictException;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserApiToken;
import net.geant.nmaas.portal.persistence.repositories.UserApiTokenRepository;
import net.geant.nmaas.portal.service.CustomAccessTokenService;
import net.geant.nmaas.portal.service.impl.security.SecretPasswordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomAccessTokenServiceImpl implements CustomAccessTokenService {

    private final UserApiTokenRepository userApiTokenRepository;

    private final SecretPasswordService secretPasswordService;

    @Override
    public void invalidate(Long id) {
        UserApiToken token = findToken(id);
        token.setValid(false);
        userApiTokenRepository.save(token);
    }

    @Override
    public void delete(Long id) {
        UserApiToken token = findToken(id);
        if (!token.isValid()) {
            token.setDeleted(true);
            userApiTokenRepository.save(token);
        } else {
            throw new IllegalArgumentException("Token is still valid, can not delete valid token");
        }
    }

    @Override
    public UserApiTokenDto createToken(User user, String name) {
        List<UserApiToken> tokens = userApiTokenRepository.findAllByUserIdAndName(user.getId(), name);
        if (!tokens.isEmpty() && tokens.stream().anyMatch(c -> !c.isDeleted())) {
            throw new DataConflictException("Token name is already in use.");
        }

        UserApiToken token = createNewToken(user, name);
        String hashedValued = secretPasswordService.hashSecret(token.getTokenValue());
        UserApiTokenDto view = mapToView(token);
        token.setTokenValue(hashedValued);
        log.warn("Token value is: {}, hashed: {}", view.tokenValue(), hashedValued);
        userApiTokenRepository.save(token);
        return view;
    }

    @Override
    public List<UserApiTokenDto> getAll(Long userId) {
        return userApiTokenRepository.findAllByUserId(userId).stream()
                .filter(userApiToken -> !userApiToken.isDeleted())
                .map(this::mapToView)
                .toList();
    }

    private UserApiToken createNewToken(User user, String name) {
        UserApiToken token = new UserApiToken();
        token.setName(name);
        token.setUser(user);
        token.setTokenValue(generateToken());
        token.setValid(true);
        token.setDeleted(false);
        return token;
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private UserApiToken findToken(Long id) {
        return userApiTokenRepository
                .findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Could not find access token with id: " + id));
    }

    private UserApiTokenDto mapToView(UserApiToken token) {
        return new UserApiTokenDto(token.getId(), token.getName(), token.getTokenValue(),
                token.isValid(), token.isDeleted());
    }

}
