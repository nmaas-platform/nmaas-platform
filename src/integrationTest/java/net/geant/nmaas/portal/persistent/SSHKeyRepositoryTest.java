package net.geant.nmaas.portal.persistent;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.SSHKeyRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Log4j2
public class SSHKeyRepositoryTest {

    private static final String DOMAIN = "userdom";
    private static final String VALID_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDuQ6IUs8q207aA/q+KRswa+Ui+hx2c8yN/EoSIGCRhoadKkn1dN1GCGr6hn4te7BvWunGuRbLxtKf23IQvud3NuhWVrNCwJbHOIJ3To+45IBnGuur7u5CDBPR8tsvbkk4jde8j58K2xM+9GeGBxZhXEvgVs+uQwDqMhHeWCS9sqcf0Es0fXlQOffQCEiRnGOrd7cL1iIr7fimqGrGYmqxu3gfzhEPrMNHoXW5QArne48gK0EZvxmMoP5FWXLQx3itzDKfPaIB//uRBbBTNFUd6FWjZs2S1vsmKbV7LU0BBRu+CLfbw41eFuQUbx2/hQc+JbV0E5l31oCi04cZtfr1CKvmmA4t13UyooCPZWafS/uBi8n8eVoOT+VisEhbsFQJydulWeEeFF5bIwrMxPx4SucmvnsgZouemHSpuLvwIFanycPc6PWDL7gx6MLbLHulvNO22FVdRnuisgspGM85H1WFD51L5ARUz/bTltbYRKtcXhi3lYAETPmHjdiQCOp9pWNTTs+JHTz1mfA7LSVoceWO+5mdMEGwH3sEeZ/PgK6rUBocEV+xP7nj+i2L+KS/c+NvC49etjHiGCxUfXZozNSoma/tkSav2tvx10DWG8Yb93CAyqSyW1VdQIE/jE0PNWWwhvDzj1td4qsJw2+x8bCZVUChf50WxuEtBAFzVjw== user@vm1"; // user@vm1

    @Autowired
    UserRepository userRepository;

    @Autowired
    DomainRepository domainRepository;

    @Autowired
    SSHKeyRepository repository;

    @BeforeEach
    @Transactional
    void setUp() {
        domainRepository.save(new Domain(DOMAIN, DOMAIN, true));
        User tester = new User("tester", true, "test123", domainRepository.findByName(DOMAIN).get(), Role.ROLE_USER);
        tester.setEmail("test@test.com");
        tester = userRepository.save(tester);

        SSHKeyEntity key = new SSHKeyEntity(tester, "my_new_key", VALID_KEY);
        repository.save(key);
    }

    @AfterEach
    void tearDown(){
        try{
            this.userRepository.findAll().stream()
                    .filter(user -> !user.getUsername().equalsIgnoreCase(UsersHelper.ADMIN.getUsername()))
                    .forEach(user -> userRepository.delete(user));
            domainRepository.findAll().stream()
                    .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                    .forEach(domain -> domainRepository.delete(domain));
            repository.deleteAll();
        } catch(Exception ex){
            log.error(ex.getMessage());
        }
    }

    @Test
    void afterKeyIsDeletedUserShouldRemain() {
        User owner = userRepository.findByUsername("tester").get();
        SSHKeyEntity key = repository.findAllByOwner(owner).get(0);

        repository.deleteById(key.getId());
        assertEquals(1, userRepository.count());
        boolean exists = userRepository.existsByUsername("tester");
        assertTrue(exists);
    }

    @Test
    void whenUserDeletedKeysShouldBeDeleted() {
        User owner = userRepository.findByUsername("tester").get();
        userRepository.delete(owner);
        assertFalse(userRepository.existsByUsername(owner.getUsername())); // the user is no more

        assertEquals(0, repository.findAllByOwner(owner).size());
    }
}
