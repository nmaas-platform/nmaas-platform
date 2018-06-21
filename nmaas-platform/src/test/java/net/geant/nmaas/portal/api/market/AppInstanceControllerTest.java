package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.portal.BaseControllerTest;
import net.geant.nmaas.portal.persistent.entity.*;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)

public class AppInstanceControllerTest extends BaseControllerTest {

    @MockBean
    private ApplicationInstanceService instances;

    @MockBean
    private AppLifecycleManager appLifecycleManager;

    @Before
    public void setup(){
        this.mvc = this.createMVC();
    }

    @Test
    public void shouldRestartApplication() throws Exception{
        Domain domain = UsersHelper.DOMAIN1;
        User user = UsersHelper.ADMIN;
        AppInstance appInstance = new AppInstance(new Application("test"),"test",domain,user);
        Mockito.when(instances.find(1L)).thenReturn(Optional.of(appInstance));
        mvc.perform(post("/api/apps/instances/{appInstanceId}/restart", 1L)
        .header("Authorization","Bearer " + getValidTokenForUser(user))).andExpect(status().isOk());
        Mockito.verify(appLifecycleManager, times(1)).restartApplication(appInstance.getInternalId());
        mvc.perform(post("/api/domains/{domainId}/apps/instances/{appInstanceId}/restart",domain.getId(),1L)
                .header("Authorization","Bearer " + getValidTokenForUser(user))).andExpect(status().isOk());
        Mockito.verify(appLifecycleManager,times(2)).restartApplication(appInstance.getInternalId());
    }

    @Test
    public void shouldThrowAnExceptionWhenInputIsIncorrect() throws Exception{
        Mockito.when(instances.find(0L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/apps/instances/{appInstanceId}/restart",0L)
                .header("Authorization","Bearer " + getValidUserTokenFor(Role.ROLE_SUPERADMIN))).andExpect(status().is4xxClientError());
    }
}
