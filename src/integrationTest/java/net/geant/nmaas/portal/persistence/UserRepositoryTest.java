package net.geant.nmaas.portal.persistence;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
public class UserRepositoryTest {

    private static final String DOMAIN = "userdom";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DomainRepository domainRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        domainRepository.findAll().stream()
                .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                .forEach(domain -> domainRepository.delete(domain));
        domainRepository.save(new Domain(DOMAIN, DOMAIN, true));
    }

    @AfterEach
    @Transactional
    void tearDown() {
        try {
            userRepository.findAll().stream()
                    .filter(user -> !user.getUsername().equalsIgnoreCase(UsersHelper.ADMIN.getUsername()))
                    .forEach(user -> userRepository.delete(user));
            domainRepository.findAll().stream()
                    .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                    .forEach(domain -> domainRepository.delete(domain));
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Test
    void shouldCreateTwoUsersOneWithRoleUserAndSecondWithRoleSystemAdminAndAddSecondUserRoleUser() {
        User tester = new User("tester", true, "test123", domainRepository.findByName(DOMAIN).get(), Role.ROLE_USER);
        tester.setEmail("test@test.com");
        User admin = new User("testadmin", true, "testadmin123", domainRepository.findByName(DOMAIN).get(), Role.ROLE_SYSTEM_ADMIN);
        admin.setEmail("admin@test.com");
        admin.getRoles().add(new UserRole(admin, domainRepository.findByName(DOMAIN).get(), Role.ROLE_USER));
        userRepository.save(tester);
        userRepository.save(admin);
        assertEquals(3, userRepository.count());

        Optional<User> adminPersisted = userRepository.findByUsername("testadmin");
        assertTrue(adminPersisted.isPresent());
        assertNotNull(adminPersisted.get().getId());
        assertEquals(2, adminPersisted.get().getRoles().size());
    }

    @Test
    @Transactional
    void shouldSetEnabledFlag() {
        User enableTestUser = new User("enableTest", false, "test123",
                domainRepository.findByName(DOMAIN).orElseThrow(), Role.ROLE_USER);
        enableTestUser.setEmail("enableUser@test.com");
        userRepository.save(enableTestUser);

        Optional<User> enableTestUserFalse = userRepository.findByUsername("enableTest");
        assertNotNull(enableTestUserFalse.orElseThrow());
        assertFalse(enableTestUserFalse.get().isEnabled());

        userRepository.setEnabledFlag(enableTestUserFalse.get().getId(), true);

        Optional<User> enableTestUserTrue = userRepository.findByUsername("enableTest");
        assertNotNull(enableTestUserTrue.orElseThrow());
        assertTrue(enableTestUserTrue.get().isEnabled());
    }

    @Test
    @Transactional
    void shouldSetTermsOfUseAcceptedFlag() {
        User termsOfUseAcceptedTestUser = new User("termsTest", true, "test123",
                domainRepository.findByName(DOMAIN).orElseThrow(), Role.ROLE_USER, false, true);
        termsOfUseAcceptedTestUser.setEmail("terms@email.com");
        userRepository.save(termsOfUseAcceptedTestUser);

        Optional<User> termsOfUseAcceptedTestUserFalse = userRepository.findByUsername("termsTest");
        assertNotNull(termsOfUseAcceptedTestUserFalse.orElseThrow());
        assertFalse(termsOfUseAcceptedTestUserFalse.get().isTermsOfUseAccepted());

        userRepository.setTermsOfUseAcceptedFlag(termsOfUseAcceptedTestUserFalse.get().getId(), true);

        Optional<User> termsOfUseAcceptedTestUserTrue = userRepository.findByUsername("termsTest");
        assertNotNull(termsOfUseAcceptedTestUserTrue.orElseThrow());
        assertTrue(termsOfUseAcceptedTestUserTrue.get().isTermsOfUseAccepted());
    }

    @Test
    @Transactional
    void shouldSetPrivacyPolicyAcceptedFlag() {
        User privacyPolicyAcceptedTestUser = new User("privacyTest", true, "test123",
                domainRepository.findByName(DOMAIN).orElseThrow(), Role.ROLE_USER, true, false);
        privacyPolicyAcceptedTestUser.setEmail("privacy@test.com");
        userRepository.save(privacyPolicyAcceptedTestUser);

        Optional<User> privacyPolicyAcceptedTestUserFalse = userRepository.findByUsername("privacyTest");
        assertNotNull(privacyPolicyAcceptedTestUserFalse.orElseThrow());
        assertFalse(privacyPolicyAcceptedTestUserFalse.get().isPrivacyPolicyAccepted());

        userRepository.setPrivacyPolicyAcceptedFlag(privacyPolicyAcceptedTestUserFalse.get().getId(), true);

        Optional<User> privacyPolicyAcceptedTestUserTrue = userRepository.findByUsername("privacyTest");
        assertTrue(privacyPolicyAcceptedTestUserTrue.isPresent());
        assertTrue(privacyPolicyAcceptedTestUserTrue.get().isPrivacyPolicyAccepted());
    }

    @Test
    void shouldSaveUserWithMail() {
        User testUser = new User("testUser", true, "test123",
                domainRepository.findByName(DOMAIN).orElseThrow(), Role.ROLE_USER, true, false);
        testUser.setEmail("email@email.com");
        User result = userRepository.save(testUser);
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void shouldNotSaveUserWithWrongMailFormat() {
        assertThrows(ConstraintViolationException.class, () -> {
            User testUser = new User("testUser", true, "test123",
                    domainRepository.findByName(DOMAIN).orElseThrow(), Role.ROLE_USER, true, false);
            testUser.setEmail("emailemail.com");
            userRepository.save(testUser);
        });
    }

    @Test
    void shouldSaveUserWithSamlToken() {
        User testUser = new User("testUser", true, "test123",
                domainRepository.findByName(DOMAIN).orElseThrow(), Role.ROLE_USER, true, false);
        testUser.setSamlToken("test|1234|saml");
        User result = userRepository.save(testUser);
        assertEquals(testUser.getSamlToken(), result.getSamlToken());
    }

    @Test
    void shouldNotSaveUserWithoutBothMailAndToken() {
        assertThrows(ConstraintViolationException.class, () -> {
            User testUser = new User("testUser", true, "test123",
                    domainRepository.findByName(DOMAIN).orElseThrow(), Role.ROLE_USER, true, false);
            userRepository.save(testUser);
        });
    }

    @Test
    void shouldNotSaveUserWithNonUnique() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            User testUser = new User("testUser", true, "test123",
                    domainRepository.findByName(DOMAIN).orElseThrow(), Role.ROLE_USER, true, false);
            testUser.setEmail("test@test.com");
            userRepository.save(testUser);
            User testUser2 = new User("testUser2", true, "test123",
                    domainRepository.findByName(DOMAIN).orElseThrow(), Role.ROLE_USER, true, false);
            testUser2.setEmail("test@test.com");
            userRepository.save(testUser2);
        });
    }

}