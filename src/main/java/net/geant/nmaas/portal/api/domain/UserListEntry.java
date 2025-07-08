package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserListEntry extends UserBase implements Serializable {

    protected String name;
    protected String email;

    protected OffsetDateTime lastSuccessfulLoginDate;
    protected OffsetDateTime firstLoginDate;

    protected String globalRole;
    protected Set<String> domainsName;
    protected Role domainRole;

    public UserListEntry(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getFirstname() != null ? user.getFirstname() : "";
        if (user.getLastname() != null && !user.getLastname().isEmpty()) {
            this.name += (this.name.isEmpty() ? "" : " ") + user.getLastname();
        }
        this.email = user.getEmail();

        this.domainsName = user.getRoles().stream()
                .filter(userRole -> userRole.getDomain() != null && !Objects.equals(userRole.getDomain().getName(), "GLOBAL"))
                .map(userRole -> userRole.getDomain().getName())
                .collect(Collectors.toSet());

        List<Role> globalRoleList = List.of(Role.ROLE_GUEST, Role.ROLE_SYSTEM_ADMIN, Role.ROLE_TOOL_MANAGER, Role.ROLE_OPERATOR, Role.ROLE_GROUP_MANAGER);

        Optional<Role> globalRole = user.getRoles().stream()
                .map(UserRole::getRole)
                .filter(globalRoleList::contains).findFirst();
        this.globalRole = globalRole.map(Enum::name).orElse("");

        this.enabled = user.isEnabled();
    }

}
