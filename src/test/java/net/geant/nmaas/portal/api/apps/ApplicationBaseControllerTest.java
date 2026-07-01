package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.persistence.repositories.RatingRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.ApplicationSubscriptionService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationBaseControllerTest {

    private final ModelMapper modelMapper = new ModelMapper();

    private final ApplicationService applicationService = mock(ApplicationService.class);
    private final ApplicationBaseService applicationBaseService = mock(ApplicationBaseService.class);
    private final UserService userService = mock(UserService.class);
    private final RatingRepository ratingRepository = mock(RatingRepository.class);
    private final ApplicationSubscriptionService applicationSubscriptionService = mock(ApplicationSubscriptionService.class);

    private ApplicationBaseController controller;

    @BeforeEach
    void setup() {
        controller = new ApplicationBaseController(
                modelMapper,
                applicationService,
                applicationBaseService,
                userService,
                ratingRepository,
                applicationSubscriptionService
        );
    }

    @Test
    void shouldReturnAllBasesForSystemAdmin() {
        Principal principal = principal("admin");
        when(userService.findByUsername("admin")).thenReturn(
                Optional.of(userWithRoles("admin", Role.ROLE_SYSTEM_ADMIN)));

        ApplicationBase base1 = new ApplicationBase(1L, "app-a");
        base1.setOwner("owner-a");
        ApplicationBase base2 = new ApplicationBase(2L, "app-b");
        base2.setOwner("owner-b");

        when(applicationBaseService.findAll()).thenReturn(List.of(base1, base2));
        when(ratingRepository.getApplicationRating(1L)).thenReturn(new Integer[]{4, 5});
        when(ratingRepository.getApplicationRating(2L)).thenReturn(new Integer[]{3});

        var result = controller.getAllApplicationBaseBasedByRole(principal);

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnOnlyOwnedBasesForToolManager() {
        Principal principal = principal("tool-manager");
        when(userService.findByUsername("tool-manager"))
                .thenReturn(Optional.of(userWithRoles("tool-manager", Role.ROLE_TOOL_MANAGER)));

        ApplicationBase owned = new ApplicationBase(1L, "owned-app");
        owned.setOwner("tool-manager");
        ApplicationBase other = new ApplicationBase(2L, "other-app");
        other.setOwner("another-user");

        when(applicationBaseService.findAll()).thenReturn(List.of(owned, other));
        when(ratingRepository.getApplicationRating(1L)).thenReturn(new Integer[]{5});

        var result = controller.getAllApplicationBaseBasedByRole(principal);

        assertEquals(1, result.size());
        assertEquals("owned-app", result.getFirst().getName());
    }

    private static Principal principal(String name) {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(name);
        return principal;
    }

    private static User userWithRoles(String username, Role role) {
        User user = new User(username, true);
        Domain domain = new Domain(1L, "GLOBAL", "GLOBAL");
        user.setRoles(List.of(new UserRole(user, domain, role)));
        return user;
    }
}
