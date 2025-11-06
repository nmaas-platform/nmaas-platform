package net.geant.nmaas.portal.service.impl.security;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.AclService.Permissions;
import net.geant.nmaas.portal.service.DomainService;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class AppInstancePermissionCheck extends BasePermissionCheck {

    static final String APP_INSTANCE = "appInstance";

    private final EnumMap<Role, Permissions[]> globalPermMatrix = new EnumMap<>(Role.class);
    private final EnumMap<Role, Permissions[]> permMatrix = new EnumMap<>(Role.class);

    private final AppInstanceRepository appInstanceRepository;

    private final DomainService domainService;

    public AppInstancePermissionCheck(AppInstanceRepository appInstanceRepository, DomainService domainService) {
        super();
        this.appInstanceRepository = appInstanceRepository;
        this.domainService = domainService;
        this.setupMatrix();
    }

    @Override
    protected void setupMatrix() {
        globalPermMatrix.put(Role.ROLE_SYSTEM_ADMIN, new Permissions[]{Permissions.CREATE, Permissions.DELETE, Permissions.OWNER, Permissions.READ, Permissions.WRITE});
        globalPermMatrix.put(Role.ROLE_OPERATOR, new Permissions[]{Permissions.READ});
        globalPermMatrix.put(Role.ROLE_TOOL_MANAGER, new Permissions[]{Permissions.READ});
        globalPermMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[]{});
        globalPermMatrix.put(Role.ROLE_USER, new Permissions[]{});
        globalPermMatrix.put(Role.ROLE_GUEST, new Permissions[]{});
        globalPermMatrix.put(Role.ROLE_GROUP_DOMAIN_ADMIN, new Permissions[]{});
        globalPermMatrix.put(Role.ROLE_GROUP_MANAGER, new Permissions[]{});

        permMatrix.put(Role.ROLE_DOMAIN_ADMIN, new Permissions[]{Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
        permMatrix.put(Role.ROLE_GROUP_DOMAIN_ADMIN, new Permissions[]{Permissions.CREATE, Permissions.DELETE, Permissions.READ, Permissions.WRITE, Permissions.OWNER});
        permMatrix.put(Role.ROLE_USER, new Permissions[]{Permissions.READ});
        permMatrix.put(Role.ROLE_GUEST, new Permissions[]{});
        permMatrix.put(Role.ROLE_GROUP_MANAGER, new Permissions[]{Permissions.READ});
    }

    @Override
    public boolean supports(String targetType) {
        return APP_INSTANCE.equalsIgnoreCase(targetType);
    }

    @Override
    protected Set<Permissions> evaluatePermissions(User user, Serializable targetId, String targetType) {
        Validate.notNull(targetId, "targetId is missing");
        Validate.isTrue(targetId instanceof Long, "targetId is not a valid type of " + Long.class.getSimpleName());
        Validate.isTrue(APP_INSTANCE.equalsIgnoreCase(targetType), "targetType not supported");
        Validate.isTrue(user != null, "user is missing");

        Set<Permissions> resultPerms = new HashSet<>();

        Optional<AppInstance> appInstance = appInstanceRepository.findById((Long) targetId);

        if (appInstance.isPresent()) {
            Domain domain = (appInstance.get().getDomain() != null
                    ? appInstance.get().getDomain()
                    : domainService.getGlobalDomain().orElse(null));

            for (UserRole role : user.getRoles()) {
                if (role.getDomain() == null) {
                    continue;
                }
                if (role.getDomain().equals(domain)) {
                    resultPerms.addAll(Arrays.asList(permMatrix.get(role.getRole())));
                } else if (role.getDomain().equals(domainService.getGlobalDomain().orElse(null))) {
                    resultPerms.addAll(Arrays.asList(globalPermMatrix.get(role.getRole())));
                }
            }

            // explicitly add READ permission if user is member of the app instance
            if (appInstance.get().getMembers().contains(user)) {
                resultPerms.add(Permissions.READ);
            }
        }
        return resultPerms;
    }

}
