package net.geant.nmaas.portal.api.maintenance;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MaintenanceControllerTest extends BaseControllerTest {

    @Autowired
    private MaintenanceManager maintenanceManager;

    private MockMvc mvc;

    private static String URL_PREFIX = "/api/maintenance";

    @Before
    public void init(){
        mvc = createMVC();
    }

    @Test
    public void shouldUpdateBooleanFlag() throws Exception {
        User user = UsersHelper.ADMIN;
        Maintenance maintenance = new Maintenance(false);
        mvc.perform(post(URL_PREFIX)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user))
                .content(new ObjectMapper().writeValueAsString(maintenance))
                                        .accept(MediaType.APPLICATION_JSON));
        MvcResult mvcResult = mvc.perform(get(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andReturn();
        assertThat(mvcResult.getResponse().getContentAsString(), mvcResult.getResponse().getContentAsString().equals("{\"maintenance\":false}"));
    }
}
