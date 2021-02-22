package net.geant.nmaas.portal.service.impl.security;

import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.AclService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AclServiceTest {

    private UserService userService = mock(UserService.class);

    private AclServiceImpl service = null;

    private DomainObjectPermissionCheck domain = mock(DomainObjectPermissionCheck.class);
    private CommentPermissionCheck comment = mock(CommentPermissionCheck.class);
    private AppInstancePermissionCheck instance = mock(AppInstancePermissionCheck.class);
    private AppTemplatePermissionCheck template = mock(AppTemplatePermissionCheck.class);

    @BeforeEach
    public void setup() {

        User user = new User("user");
        Domain d = new Domain("domain-one", "code-1");
        UserRole userRole = new UserRole(user, d, Role.ROLE_GUEST);
        Set<UserRole> roleSet = new HashSet<>();
        roleSet.add(userRole);
        user.setNewRoles(roleSet);
        when(userService.findById(-1L)).thenReturn(Optional.empty());
        when(userService.findById(1L)).thenReturn(Optional.of(user));

        service = new AclServiceImpl(domain, comment, instance, template, userService);
    }

    @Test
    public void shouldAuthorizeAccessWithTemplatePermissionCheck() {
        String targetType = "template";
        when(domain.supports(targetType)).thenReturn(false);
        when(comment.supports(targetType)).thenReturn(false);
        when(instance.supports(targetType)).thenReturn(false);
        when(template.supports(targetType)).thenReturn(true);

        when(template.check(any(), any(), any(), (AclService.Permissions[]) any())).thenReturn(true);

        assertTrue(service.isAuthorized(1L, "targetID", targetType, new AclService.Permissions[0]));
    }

    @Test
    public void shouldNotAuthorizeWhenUserNotFound() {
        assertFalse(service.isAuthorized(-1L, "targetID", "any", new AclService.Permissions[0]));
    }

    @Test
    public void shouldNotAuthorizeWhenNoPermissionChecksNoPermissions() {
        String targetType = "template";
        when(domain.supports(targetType)).thenReturn(false);
        when(comment.supports(targetType)).thenReturn(false);
        when(instance.supports(targetType)).thenReturn(false);
        when(template.supports(targetType)).thenReturn(false);

        assertFalse(service.isAuthorized(1L, "targetID", targetType, new AclService.Permissions[0]));
    }

    /**
     * after adjusting new security policy, certain default permissions were removed,
     * so this test is redundant and was disabled
     */
    @Disabled
    @Test
    public void shouldAuthorizeWhenNoPermissionChecksAndMatchingPermission() {
        String targetType = "template";
        when(domain.supports(targetType)).thenReturn(false);
        when(comment.supports(targetType)).thenReturn(false);
        when(instance.supports(targetType)).thenReturn(false);
        when(template.supports(targetType)).thenReturn(false);

        AclService.Permissions[] perms = new AclService.Permissions[1];
        perms[0] = AclService.Permissions.READ;

        assertTrue(service.isAuthorized(1L, "targetID", targetType, perms));
    }

    /**
     * after adjusting new security policy, certain default permissions were removed,
     * so this test is redundant and was disabled
     */
    @Disabled
    @Test
    public void shouldAuthorizeWhenNoPermissionChecksAndMatchingSinglePermission() {
        String targetType = "template";
        when(domain.supports(targetType)).thenReturn(false);
        when(comment.supports(targetType)).thenReturn(false);
        when(instance.supports(targetType)).thenReturn(false);
        when(template.supports(targetType)).thenReturn(false);

        assertTrue(service.isAuthorized(1L, "targetID", targetType, AclService.Permissions.READ));
    }

}
