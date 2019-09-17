package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AppInstanceControllerIntTest extends BaseControllerTestSetup {

    @MockBean
    private AppInstanceRepository appInstanceRepository;

    @MockBean
    private AppLifecycleManager appLifecycleManager;

    @BeforeEach
    public void setup(){
        this.mvc = this.createMVC();
    }

    @Test
    public void shouldRestartApplication() throws Exception{
        Domain domain = UsersHelper.DOMAIN1;
        User user = UsersHelper.ADMIN;
        AppInstance appInstance = new AppInstance(new Application("test","testVersion","admin"),"test",domain,user);
        when(appInstanceRepository.findById(1L)).thenReturn(Optional.of(appInstance));
        mvc.perform(post("/api/apps/instances/{appInstanceId}/restart", 1L)
                .header("Authorization","Bearer " + getValidTokenForUser(user)))
                .andExpect(status().isOk());
        verify(appLifecycleManager, times(1)).restartApplication(appInstance.getInternalId());
    }

    @Test
    public void shouldThrowAnExceptionWhenInputIsIncorrect() throws Exception{
        when(appInstanceRepository.findById(0L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/apps/instances/{appInstanceId}/restart",0L)
                .header("Authorization","Bearer " + getValidUserTokenFor(Role.ROLE_SYSTEM_ADMIN)))
                .andExpect(status().is(404));
    }
}
