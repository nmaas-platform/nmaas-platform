package net.geant.nmaas.portal.api.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.domain.AppRateView;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;
import net.geant.nmaas.portal.persistent.repositories.RatingRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class RatingControllerTest extends BaseControllerTestSetup {

    @Autowired
    private ApplicationRepository appRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private RatingRepository ratingRepo;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup(){
        this.mvc = createMVC();
        this.appRepo.save(new Application("testAPP", "1.1.0", "admin"));
    }

    @AfterEach
    public void teardown(){
        this.ratingRepo.deleteAll();
        this.appRepo.deleteAll();
    }

    @Test
    public void shouldSetUserAppRating() throws Exception {
        long appId = this.appRepo.findAll().get(0).getId();
        MvcResult mvcResult = mvc.perform(post("/api/apps/"+appId+"/rate/my/4")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().contains("true"));
    }

    @Test
    public void shouldNormalizeNegativeUserAppRating() throws Exception {
        long appId = this.appRepo.findAll().get(0).getId();
        MvcResult mvcResult = mvc.perform(post("/api/apps/"+appId+"/rate/my/-1")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().contains("true"));
    }

    @Test
    public void shouldNormalizeUserAppRatingOverMax() throws Exception {
        long appId = this.appRepo.findAll().get(0).getId();
        MvcResult mvcResult = mvc.perform(post("/api/apps/"+appId+"/rate/my/25")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().contains("true"));
    }

    @Test
    public void shouldGetUserAppRating() throws Exception {
        long appId = this.appRepo.findAll().get(0).getId();
        mvc.perform(post("/api/apps/"+appId+"/rate/my/4")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        long userId = this.userRepo.findAll().get(0).getId();
        MvcResult mvcResult = mvc.perform(get("/api/apps/"+appId+"/rate/user/"+userId)
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        AppRateView result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AppRateView.class);
        assertEquals(4, result.getRate());
        assertEquals(4.0, result.getAverageRate(), 0.1);
    }

    @Test
    public void shouldGetMyAppRating() throws Exception {
        long appId = this.appRepo.findAll().get(0).getId();
        mvc.perform(post("/api/apps/"+appId+"/rate/my/4")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        MvcResult mvcResult = mvc.perform(get("/api/apps/"+appId+"/rate/my/")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        AppRateView result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AppRateView.class);
        assertEquals(4, result.getRate());
        assertEquals(4.0, result.getAverageRate(), 0.1);
    }

    @Test
    public void shouldGetAppRating() throws Exception {
        long appId = this.appRepo.findAll().get(0).getId();
        mvc.perform(post("/api/apps/"+appId+"/rate/my/5")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        MvcResult mvcResult = mvc.perform(get("/api/apps/"+appId+"/rate")
                .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        AppRateView result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AppRateView.class);
        assertEquals(5.0, result.getAverageRate(), 0.1);
    }
}
