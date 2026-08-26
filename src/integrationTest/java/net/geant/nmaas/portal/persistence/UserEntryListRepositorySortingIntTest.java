package net.geant.nmaas.portal.persistence;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegisterType;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.UserEntryListRepository;
import net.geant.nmaas.portal.persistence.repositories.UserLoginRegisterRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.UserListEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
public class UserEntryListRepositorySortingIntTest {

    private static final String DOMAIN = "sortdom";

    @Autowired
    UserRepository userRepository;

    @Autowired
    DomainRepository domainRepository;

    @Autowired
    UserLoginRegisterRepository userLoginRegisterRepository;

    @Autowired
    UserEntryListRepository userEntryListRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        if (!domainRepository.existsByCodename(DOMAIN)) {
            domainRepository.save(new Domain(DOMAIN, DOMAIN, true));
        }
        Domain domain = domainRepository.findByName(DOMAIN).get();

        // never logged in -> null first/last login dates
        User neverLoggedIn = new User("neverloggedin", true, "pass123", domain, Role.ROLE_USER);
        neverLoggedIn.setEmail("never@test.com");

        // logged in once recently
        User recentLogin = new User("recentlogin", true, "pass123", domain, Role.ROLE_USER);
        recentLogin.setEmail("recent@test.com");

        // logged in once long ago
        User oldLogin = new User("oldlogin", true, "pass123", domain, Role.ROLE_USER);
        oldLogin.setEmail("old@test.com");

        userRepository.save(neverLoggedIn);
        userRepository.save(recentLogin);
        userRepository.save(oldLogin);

        OffsetDateTime now = OffsetDateTime.now();
        userLoginRegisterRepository.save(new UserLoginRegister(now.minusDays(1), recentLogin, UserLoginRegisterType.SUCCESS, null, null, null));
        userLoginRegisterRepository.save(new UserLoginRegister(now.minusYears(2), oldLogin, UserLoginRegisterType.SUCCESS, null, null, null));
    }

    @AfterEach
    @Transactional
    void tearDown() {
        userLoginRegisterRepository.deleteAllInBatch();
        userLoginRegisterRepository.flush();
        try {
            userRepository.findAll().stream()
                    .filter(user -> !user.getUsername().equalsIgnoreCase("admin"))
                    .forEach(userRepository::delete);
            domainRepository.findAll().stream()
                    .filter(domain -> !domain.getCodename().equalsIgnoreCase("global"))
                    .forEach(domainRepository::delete);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @Test
    void shouldSortByFirstLoginDateAscendingWithNullsFirst() {
        Page<UserListEntry> page = userEntryListRepository.findAll(
                (String) null,
                PageRequest.of(0, 50, Sort.by(new Sort.Order(Sort.Direction.ASC, "firstLoginDate").nullsFirst())));
        List<UserListEntry> entries = page.getContent();

        log.info("ASC firstLoginDate (nulls first) order: {}", entries.stream().map(e -> e.getUsername() + "=" + e.getFirstLoginDate()).toList());

        // users with null firstLoginDate (never logged in) must sort first (treated as oldest),
        // before users that have actually logged in
        int firstNonNullIndex = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getFirstLoginDate() != null) {
                firstNonNullIndex = i;
                break;
            }
        }
        // all null entries precede all non-null entries
        for (int i = 0; i < firstNonNullIndex; i++) {
            assertNull(entries.get(i).getFirstLoginDate(), "null firstLoginDate must sort before non-null at index " + i);
        }
        // old login (2 years ago) before recent login (1 day ago)
        assertEquals("oldlogin", entries.get(firstNonNullIndex).getUsername());
        assertEquals("recentlogin", entries.get(firstNonNullIndex + 1).getUsername());
    }

    @Test
    void shouldSortByLastSuccessfulLoginDateDescendingWithNullsLast() {
        // "null = oldest": under DESC (newest first) nulls must land last -> NULLS LAST
        Page<UserListEntry> page = userEntryListRepository.findAll(
                (String) null,
                PageRequest.of(0, 50, Sort.by(new Sort.Order(Sort.Direction.DESC, "lastSuccessfulLoginDate").nullsLast())));
        List<UserListEntry> entries = page.getContent();

        log.info("DESC lastSuccessfulLoginDate (nulls last) order: {}", entries.stream().map(e -> e.getUsername() + "=" + e.getLastSuccessfulLoginDate()).toList());

        // most recent login first
        assertEquals("recentlogin", entries.get(0).getUsername());
        assertNotNull(entries.get(0).getLastSuccessfulLoginDate());
        // never-logged-in users (null) sort last, since nulls are treated as oldest
        assertNull(entries.get(entries.size() - 1).getLastSuccessfulLoginDate());
    }

    @Test
    void shouldLoadAllThreeUsers() {
        Page<UserListEntry> page = userEntryListRepository.findAll((String) null, PageRequest.of(0, 50, Sort.by("id")));
        // 3 created here + 1 admin seeded by DefaultUsersInit
        assertEquals(4, page.getTotalElements());
    }
}
