package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegisterType;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserLoginRegisterRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserLoginRegisterRepository userLoginRegisterRepository;

    /**
     * MainConfig enables caching, while the JPA test slice does not configure
     * a CacheManager; a no-op one satisfies the cache infrastructure.
     */
    @TestConfiguration(proxyBeanMethods = false)
    static class CacheManagerConfig {

        @Bean
        CacheManager cacheManager() {
            return new NoOpCacheManager();
        }

    }

    /**
     * The user association of a login history entry is read-only (the foreign
     * key value is provided by userId) and must not cascade: with
     * cascade = CascadeType.ALL the in-memory only ROLE_NOT_ACCEPTED role
     * assigned to the user during login was persisted together with the
     * login history entry.
     */
    @Test
    void shouldNotPersistUserRolesWhenSavingLoginRegister() {
        Domain global = new Domain("Global", "GLOBAL");
        entityManager.persist(global);

        User user = new User("cascadetest", true, "password", global, Role.ROLE_USER, true, true);
        user.setEmail("cascadetest@example.org");
        userRepository.saveAndFlush(user);

        User userWithPendingApprovals = userRepository.findById(user.getId()).orElseThrow();
        entityManager.clear();
        userWithPendingApprovals.setNewRoles(Set.of(new UserRole(userWithPendingApprovals, global, Role.ROLE_NOT_ACCEPTED)));

        userLoginRegisterRepository.saveAndFlush(new UserLoginRegister(OffsetDateTime.now(), userWithPendingApprovals, UserLoginRegisterType.SUCCESS, "127.0.0.1", "localhost", "test-agent"));

        entityManager.clear();
        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(reloaded.getRoles().stream().noneMatch(userRole -> userRole.getRole() == Role.ROLE_NOT_ACCEPTED),
                "in-memory only ROLE_NOT_ACCEPTED role must not be persisted through the login register entry");
    }

}
