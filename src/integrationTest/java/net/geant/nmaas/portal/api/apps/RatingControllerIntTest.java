package net.geant.nmaas.portal.api.apps;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.api.dto.applications.AppRateDto;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistence.entity.AppRate;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import net.geant.nmaas.portal.persistence.repositories.RatingRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class RatingControllerIntTest extends BaseControllerTestSetup {

    private static final long TEST_APP_ID = 5L;
    private static final long ADMIN_USER_ID = 1L;

    @MockitoBean
    private ApplicationBaseService appBaseService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RatingRepository ratingRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mvc = createMVC();
        when(appBaseService.getBaseApp(TEST_APP_ID)).thenReturn(getTestApp());
        when(userService.findByUsername(ADMIN_USERNAME)).thenReturn(Optional.of(UsersHelper.ADMIN));
        when(userService.findById(ADMIN_USER_ID)).thenReturn(Optional.of(UsersHelper.ADMIN));
    }

    @Test
    void shouldSetUserAppRating() throws Exception {
        MvcResult mvcResult = mvc.perform(post("/api/apps/" + TEST_APP_ID + "/rate/my/4")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().contains("true"));
    }

    @Test
    void shouldNormalizeNegativeUserAppRating() throws Exception {
        MvcResult mvcResult = mvc.perform(post("/api/apps/" + TEST_APP_ID + "/rate/my/-1")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().contains("true"));
    }

    @Test
    void shouldNormalizeUserAppRatingOverMax() throws Exception {
        MvcResult mvcResult = mvc.perform(post("/api/apps/" + TEST_APP_ID + "/rate/my/25")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(mvcResult.getResponse().getContentAsString().contains("true"));
    }

    @Test
    void shouldGetUserAppRating() throws Exception {
        AppRate.AppRateId appRateId = new AppRate.AppRateId(TEST_APP_ID, ADMIN_USER_ID);
        AppRate appRate = new AppRate(appRateId);
        appRate.setRate(4);
        when(ratingRepository.findById(appRateId)).thenReturn(Optional.of(appRate));
        when(ratingRepository.getApplicationRating(TEST_APP_ID)).thenReturn(List.of(1, 4, 4).toArray(Integer[]::new));

        MvcResult mvcResult = mvc.perform(get("/api/apps/" + TEST_APP_ID + "/rate/user/" + ADMIN_USER_ID)
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        AppRateDto result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AppRateDto.class);
        assertEquals(4, result.getRate());
        assertEquals(3.0, result.getAverageRate(), 0.1);
    }

    @Test
    void shouldGetMyAppRating() throws Exception {
        AppRate.AppRateId appRateId = new AppRate.AppRateId(TEST_APP_ID, ADMIN_USER_ID);
        AppRate appRate = new AppRate(appRateId);
        appRate.setRate(4);
        when(ratingRepository.findById(appRateId)).thenReturn(Optional.of(appRate));
        when(ratingRepository.getApplicationRating(TEST_APP_ID)).thenReturn(List.of(1, 2, 3).toArray(Integer[]::new));

        MvcResult mvcResult = mvc.perform(get("/api/apps/" + TEST_APP_ID + "/rate/my")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        AppRateDto result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AppRateDto.class);
        assertEquals(4, result.getRate());
        assertEquals(2.0, result.getAverageRate(), 0.1);
    }

    @Test
    void shouldGetAppRating() throws Exception {
        AppRate.AppRateId appRateId = new AppRate.AppRateId(TEST_APP_ID, ADMIN_USER_ID);
        AppRate appRate = new AppRate(appRateId);
        appRate.setRate(4);
        when(ratingRepository.findById(appRateId)).thenReturn(Optional.of(appRate));
        when(ratingRepository.getApplicationRating(TEST_APP_ID)).thenReturn(List.of(1, 2, 3).toArray(Integer[]::new));

        MvcResult mvcResult = mvc.perform(get("/api/apps/" + TEST_APP_ID + "/rate")
                        .header("Authorization", "Bearer " + getValidTokenForUser(UsersHelper.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        AppRateDto result = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), AppRateDto.class);
        assertEquals(2.0, result.getAverageRate(), 0.1);
    }

    private ApplicationBase getTestApp() {
        ApplicationBase testApp = new ApplicationBase("testAPP");
        testApp.setId(5L);
        testApp.setOwner("admin");
        return testApp;
    }

}
