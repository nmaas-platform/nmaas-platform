package net.geant.nmaas.portal.api.market;

import java.util.Optional;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.UserService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;

public class AppBaseControllerTest {

    private ModelMapper modelMapper = new ModelMapper();

    private ApplicationService appService = mock(ApplicationService.class);

    private UserService userService = mock(UserService.class);

    private ApplicationBaseService appBaseService = mock(ApplicationBaseService.class);

    private AppBaseController appBaseController;

    private Application app = new Application(1L, "defaultApp", "1.1");

    private User user = new User("admin", true);

    @BeforeEach
    void setup(){
        this.appBaseController = new AppBaseController(modelMapper, appService, appBaseService, userService);
    }

    @Test
    void shouldGetApp(){
        when(appService.findApplication(anyLong())).thenReturn(Optional.of(app));
        Application result = this.appBaseController.getApp(1L);
        assertEquals(app.getName(), result.getName());
        assertEquals(app.getVersion(), result.getVersion());
    }

    @Test
    void shouldNotGetAppWhenNullId(){
        assertThrows(MissingElementException.class, () -> this.appBaseController.getApp(null));
    }

    @Test
    void shouldNotGetNotExistingApp(){
        when(appService.findApplication(anyLong())).thenReturn(Optional.empty());
        assertThrows(MissingElementException.class, () -> this.appBaseController.getApp(1L));
    }

    @Test
    void shouldGetUserByUsername(){
        when(userService.findByUsername("admin")).thenReturn(Optional.of(user));
        User admin = appBaseController.getUser("admin");
        assertEquals(user.getUsername(), admin.getUsername());
    }

    @Test
    void shouldNotGetUserByNullUsername(){
        assertThrows(MissingElementException.class, () -> appBaseController.getUser((String) null));
    }

    @Test
    void shouldNotGetNotExistingUserByUsername(){
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(MissingElementException.class, () -> appBaseController.getUser("user1"));
    }

    @Test
    void shouldGetUserById(){
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        User admin = appBaseController.getUser(1L);
        assertEquals(user.getUsername(), admin.getUsername());
    }

    @Test
    void shouldNotGetUserByNullId(){
        assertThrows(MissingElementException.class, () -> appBaseController.getUser((Long) null));
    }

    @Test
    void shouldNotGetNotExistingUserById(){
        when(userService.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(MissingElementException.class, () -> appBaseController.getUser(1L));
    }

}
