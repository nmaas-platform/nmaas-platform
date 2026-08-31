package net.geant.nmaas.portal.persistence;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.DomainGroupRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Slf4j
public class DomainGroupRepositorySortingIntTest {

    private static final String DOMAIN = "groupsortdom";

    @Autowired
    DomainGroupRepository domainGroupRepository;

    @Autowired
    DomainRepository domainRepository;

    @Autowired
    UserRepository userRepository;

    private DomainGroup gamma;
    private DomainGroup alpha;
    private DomainGroup beta;
    private User manager;

    @BeforeEach
    @Transactional
    void setUp() {
        if (!domainRepository.existsByCodename(DOMAIN)) {
            domainRepository.save(new Domain(DOMAIN, DOMAIN, true));
        }
        Domain domain = domainRepository.findByCodename(DOMAIN).orElseThrow();

        // inserted out of alphabetical order to prove the result is sorted, not insertion-ordered
        gamma = domainGroupRepository.save(new DomainGroup("Gamma Group", "gamma"));
        alpha = domainGroupRepository.save(new DomainGroup("alpha group", "alpha"));
        beta = domainGroupRepository.save(new DomainGroup("Beta Group", "beta"));

        manager = new User("groupsortmanager", true, "pass123", domain, Role.ROLE_USER);
        manager.setEmail("groupsortmanager@nmaas.test");
        manager = userRepository.save(manager);

        alpha.setManagers(List.of(manager));
        beta.setManagers(List.of(manager));
        domainGroupRepository.save(alpha);
        domainGroupRepository.save(beta);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        try {
            domainGroupRepository.findAll().stream()
                    .filter(g -> !g.getCodename().equalsIgnoreCase("global"))
                    .forEach(g -> {
                        g.setManagers(List.of());
                        domainGroupRepository.save(g);
                    });
            domainGroupRepository.flush();
            domainGroupRepository.findAll().stream()
                    .filter(g -> !g.getCodename().equalsIgnoreCase("global"))
                    .forEach(domainGroupRepository::delete);
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
    void shouldSortAllDomainGroupsByNameCaseInsensitively() {
        List<DomainGroup> groups = domainGroupRepository.findAllWithSearch(null);

        log.info("findAllWithSearch order: {}", groups.stream().map(DomainGroup::getName).toList());

        // case-insensitive ascending: "alpha group", "Beta Group", "Gamma Group"
        assertEquals(List.of("alpha group", "Beta Group", "Gamma Group"),
                groups.stream().map(DomainGroup::getName).collect(Collectors.toList()));
    }

    @Test
    void shouldSortManagerDomainGroupsByNameCaseInsensitively() {
        List<DomainGroup> groups = domainGroupRepository.findAllByManagersWithSearch(manager, null);

        log.info("findAllByManagersWithSearch order: {}", groups.stream().map(DomainGroup::getName).toList());

        // only alpha and beta are managed; sorted case-insensitively ascending
        assertEquals(List.of("alpha group", "Beta Group"),
                groups.stream().map(DomainGroup::getName).collect(Collectors.toList()));
    }

}
