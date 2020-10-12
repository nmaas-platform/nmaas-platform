package net.geant.nmaas.portal.api.profile;

import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ProfileControllerTest extends BaseControllerTestSetup {

    @BeforeEach
    void setup(){
        mvc = createMVC();
    }

    @Test
    void shouldGetUserProfile() {
        User user = UsersHelper.ADMIN;
        assertDoesNotThrow(() -> {
            mvc.perform(get("/api/profile/user")
                    .header("Authorization", "Bearer " + getValidTokenForUser(user)))
                    .andExpect(status().isOk());
        });
    }

}
