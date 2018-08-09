package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.api.auth.UserToken;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@Transactional(value=TxType.REQUIRES_NEW)
@Rollback
public class UsersControllerIntTest extends BaseControllerTest {

    final static String DOMAIN = "DOMAIN";

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UsersController userController;

    @Autowired
    private DomainService domains;

    @Autowired
    private JWTTokenService jwtTokenService;

    private String token;

    @Before
    public void setUp() throws Exception {
        mvc = createMVC();

        domains.createGlobalDomain();
        domains.createDomain(DOMAIN, DOMAIN);

        //Add extra users, default admin is already there
        User admin = userRepo.save(new User("manager", true, "manager", domains.getGlobalDomain().get(), Arrays.asList(Role.ROLE_SUPERADMIN)));

        UserToken userToken = new UserToken(jwtTokenService.getToken(admin), jwtTokenService.getRefreshToken(admin));
        token = userToken.getToken();

        prepareSecurity();
    }

    @Test
    public void testDisableUser() throws Exception {
        User user1 = userRepo.save(new User("user1", true, "user1", domains.findDomain(DOMAIN).get(), Arrays.asList(Role.ROLE_USER)));

        MvcResult result = mvc.perform(put("/api/users/status/" + user1.getId() + "?enabled=false")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assertEquals(content, "User user1 account has been deactivated by user manager with role ROLE_SUPERADMIN.");
    }

    @Test
    public void testEnableUser() throws Exception {
        User user1 = userRepo.save(new User("user1", false, "user1", domains.findDomain(DOMAIN).get(), Arrays.asList(Role.ROLE_USER)));

        MvcResult result =  mvc.perform(put("/api/users/status/" + user1.getId() + "?enabled=true")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertEquals(content, "User user1 account has been activated by user manager with role ROLE_SUPERADMIN.");
    }
}
