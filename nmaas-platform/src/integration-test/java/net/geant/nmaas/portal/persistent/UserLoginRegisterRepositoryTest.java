package net.geant.nmaas.portal.persistent;


import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserLoginRegisterRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes={PersistentConfig.class})
@EnableAutoConfiguration
@Transactional
@Rollback
@Log4j2
public class UserLoginRegisterRepositoryTest {

    private final static String DOMAIN = "userdom";

    @Autowired
    UserRepository userRepository;

    @Autowired
    DomainRepository domainRepository;

    @Autowired
    DomainService domains;

    @Autowired
    UserLoginRegisterRepository userLoginRegisterRepository;

    @BeforeEach
    @Transactional
    public void setUp() {
        domains.createDomain(new DomainRequest(DOMAIN, DOMAIN, true));
        User tester = new User("tester", true, "test123", domains.findDomain(DOMAIN).get(), Role.ROLE_USER);
        tester.setEmail("test@test.com");
        User admin = new User("testadmin", true, "testadmin123", domains.getGlobalDomain().orElseGet(() -> domains.createGlobalDomain()), Role.ROLE_SYSTEM_ADMIN);
        admin.setEmail("admin@test.com");
        admin.getRoles().add(new UserRole(admin, domains.findDomain(DOMAIN).get(), Role.ROLE_USER));
        userRepository.save(tester);
        userRepository.save(admin);
    }

    @AfterEach
    public void tearDown(){
        try{
            this.userRepository.findAll().stream()
                    .filter(user -> !user.getUsername().equalsIgnoreCase(UsersHelper.ADMIN.getUsername()))
                    .forEach(user -> userRepository.delete(user));
            domainRepository.findAll().stream()
                    .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                    .forEach(domain -> domainRepository.delete(domain));
        } catch(Exception ex){
            log.error(ex.getMessage());
        }
        this.userLoginRegisterRepository.deleteAll();
    }

    @Test
    public void shouldContainThreeUsers() {
        assertEquals(3, userRepository.count());
    }

    @Test
    public void shouldContainNoLoginRecords() {
        for(UserLoginRegister u: userLoginRegisterRepository.findAll()) {
            log.info(u.getUser().getUsername() + "\t" + u.getDate());
        }
        assertEquals(0, userLoginRegisterRepository.count());
    }

    @Test
    public void shouldInsertNewLoginEntry() {
        User user = userRepository.findByUsername("testadmin").get();

        UserLoginRegister ulr = new UserLoginRegister(OffsetDateTime.now(), user, UserLoginRegisterType.SUCCESS, null, null, null);
        this.userLoginRegisterRepository.save(ulr);

        assertEquals(1, userLoginRegisterRepository.count());

    }

}
