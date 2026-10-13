package net.geant.nmaas.portal.persistence;

import jakarta.transaction.Transactional;
import net.geant.nmaas.kubernetes.DummyKubernetesApiClientServiceConfig;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the database-level uniqueness of the external identity identifier
 * (saml_token column, storing the OIDC subject): a single external identity
 * may only be linked to one account.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@Rollback
@Import(DummyKubernetesApiClientServiceConfig.class)
public class UserSamlTokenUniquenessIntTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DomainService domainService;

    @Test
    public void shouldRejectSecondUserWithSameSamlToken() {
        userRepository.saveAndFlush(user("saml-unique-user-1", "saml-unique-user-1@example.com", "sub-duplicated"));

        User second = user("saml-unique-user-2", "saml-unique-user-2@example.com", "sub-duplicated");

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(second));
    }

    @Test
    public void shouldAllowMultipleUsersWithoutSamlToken() {
        userRepository.saveAndFlush(user("saml-null-user-1", "saml-null-user-1@example.com", null));
        userRepository.saveAndFlush(user("saml-null-user-2", "saml-null-user-2@example.com", null));

        assertTrue(userRepository.existsByEmail("saml-null-user-1@example.com"));
        assertTrue(userRepository.existsByEmail("saml-null-user-2@example.com"));
        assertFalse(userRepository.existsBySamlToken("sub-not-present"));
    }

    private User user(String username, String email, String samlToken) {
        User user = new User(username, true, "password", domainService.getGlobalDomain().get(), Role.ROLE_USER);
        user.setEmail(email);
        user.setSamlToken(samlToken);
        return user;
    }

}
