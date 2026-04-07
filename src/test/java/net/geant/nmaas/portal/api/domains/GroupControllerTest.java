package net.geant.nmaas.portal.api.domains;

import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.applications.ApplicationStatePerDomainView;
import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.api.dto.users.UserViewMinimal;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroupControllerTest {

    private final ModelMapper modelMapper = mock(ModelMapper.class);
    private final UserService userService = mock(UserService.class);
    private final DomainService domainService = mock(DomainService.class);
    private final DomainGroupService domainGroupService = mock(DomainGroupService.class);

    private final GroupController controller = new GroupController(modelMapper, userService, domainService, domainGroupService);

    @Test
    void shouldThrowWhenCreatingExistingDomainGroup() {
        DomainGroupDto dto = new DomainGroupDto();
        dto.setName("group");
        dto.setCodename("grp");
        when(domainGroupService.existDomainGroup("group", "grp")).thenReturn(true);

        assertThrows(ProcessingException.class, () -> controller.createDomainGroup(dto));
    }

    @Test
    void shouldCreateDomainGroupAndUpdateRoles() {
        DomainGroupDto request = new DomainGroupDto();
        request.setName("group");
        request.setCodename("grp");
        DomainGroupDto created = new DomainGroupDto();
        created.setId(10L);
        when(domainGroupService.existDomainGroup("group", "grp")).thenReturn(false);
        when(domainGroupService.createDomainGroup(request)).thenReturn(created);

        Id result = controller.createDomainGroup(request);

        assertNotNull(result);
        verify(domainService).updateRolesInDomainGroupByUsers(created);
    }

    @Test
    void shouldDenyAccessToGroupWhenUserIsNotManagerOrSystemAdmin() {
        Principal principal = () -> "user";
        User user = new User("user", true);
        user.setRoles(List.of());
        when(userService.findByUsername("user")).thenReturn(Optional.of(user));
        UserViewMinimal managerView = new UserViewMinimal();
        managerView.setId(1L);
        managerView.setUsername("manager");
        DomainGroupDto group = new DomainGroupDto();
        group.setManagers(List.of(managerView));
        when(domainGroupService.getDomainGroup(10L)).thenReturn(group);

        assertThrows(AccessDeniedException.class, () -> controller.getDomainGroup(10L, principal));
    }

    @Test
    void shouldAddDomainsToGroupByIds() {
        Domain d1 = new Domain(1L, "d1", "d1", true);
        Domain d2 = new Domain(2L, "d2", "d2", true);
        DomainGroupDto response = new DomainGroupDto();
        when(domainService.getDomains()).thenReturn(List.of(d1, d2));
        when(domainGroupService.addDomainsToGroup(List.of(d2), "g-code")).thenReturn(response);

        DomainGroupDto result = controller.addDomainsToGroup("g-code", List.of(2L));

        assertEquals(response, result);
    }

    @Test
    void shouldThrowWhenDeletingDomainFromGroupAndDomainMissing() {
        when(domainService.findDomain(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.deleteDomainFromGroup(10L, 99L));
    }

    @Test
    void shouldEnableAndDisableGroupApplications() throws AccessDeniedException {
        Principal principal = () -> "admin";
        User admin = new User("admin", true);
        Domain global = new Domain(1L, "global", "global", true);
        admin.setRoles(List.of(new UserRole(admin, global, Role.ROLE_SYSTEM_ADMIN)));
        when(userService.findByUsername("admin")).thenReturn(Optional.of(admin));

        ApplicationStatePerDomainView appState1 = mock(ApplicationStatePerDomainView.class);
        ApplicationStatePerDomainView appState2 = mock(ApplicationStatePerDomainView.class);
        when(appState1.getApplicationBaseId()).thenReturn(1L);
        when(appState2.getApplicationBaseId()).thenReturn(2L);

        DomainGroupDto group = new DomainGroupDto();
        group.setManagers(List.of());
        group.setApplicationStatePerDomain(List.of(appState1, appState2));
        when(domainGroupService.getDomainGroup(55L)).thenReturn(group);
        when(domainGroupService.updateDomainGroup(55L, group)).thenReturn(group);

        List<ApplicationStatePerDomainView> enabled = controller.enableGroupApplication(55L, List.of(1L), principal);
        List<ApplicationStatePerDomainView> disabled = controller.disableGroupApplication(55L, List.of(1L), principal);

        assertEquals(2, enabled.size());
        assertEquals(2, disabled.size());
        verify(appState1, times(1)).setEnabled(true);
        verify(appState1, times(1)).setEnabled(false);
        verify(appState2, times(0)).setEnabled(true);
        verify(appState2, times(0)).setEnabled(false);
    }
}
