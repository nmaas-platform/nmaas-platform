package net.geant.nmaas.portal.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.api.dto.users.RoleDto;
import net.geant.nmaas.api.dto.users.UserBase;
import net.geant.nmaas.api.dto.users.UserListEntryDto;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserRole;

import java.io.Serializable;
import java.time.OffsetDateTime;
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
        this(user, null);
    }

    public UserListEntry(User user,
                         OffsetDateTime lastSuccessfulLoginDate,
                         OffsetDateTime firstLoginDate) {
        this(user, null);
        this.lastSuccessfulLoginDate = lastSuccessfulLoginDate;
        this.firstLoginDate = firstLoginDate;
    }

    public UserListEntry(User user,
                         Long domainId,
                         OffsetDateTime lastSuccessfulLoginDate,
                         OffsetDateTime firstLoginDate) {
        this(user, domainId);
        this.lastSuccessfulLoginDate = lastSuccessfulLoginDate;
        this.firstLoginDate = firstLoginDate;
    }

    public UserListEntry(User user, Long domainId) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getFirstname() != null ? user.getFirstname() : "";
        if (user.getLastname() != null && !user.getLastname().isEmpty()) {
            this.name += (this.name.isEmpty() ? "" : " ") + user.getLastname();
        }
        this.email = user.getEmail();
        this.enabled = user.isEnabled();

        this.domainsName = user.getRoles().stream()
                .filter(userRole -> userRole.getDomain() != null && !Objects.equals(userRole.getDomain().getName(), "GLOBAL"))
                .map(userRole -> userRole.getDomain().getName())
                .collect(Collectors.toSet());

        List<Role> globalRoleList = List.of(Role.ROLE_GUEST, Role.ROLE_SYSTEM_ADMIN, Role.ROLE_TOOL_MANAGER, Role.ROLE_OPERATOR, Role.ROLE_GROUP_MANAGER);

        Optional<Role> globalRole = user.getRoles().stream()
                .map(UserRole::getRole)
                .filter(globalRoleList::contains).findFirst();
        this.globalRole = globalRole.map(Enum::name).orElse("");

        if (Objects.nonNull(domainId)) {
            Optional<Role> domainRole = user.getRoles().stream()
                    .filter(r -> r.getDomain().getId().equals(domainId))
                    .map(UserRole::getRole)
                    .findFirst();
            this.domainRole = domainRole.orElse(null);
        }
    }

    public UserListEntryDto toDto() {
        UserListEntryDto dto = new UserListEntryDto();
        dto.setId(id);
        dto.setName(name);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setLastSuccessfulLoginDate(lastSuccessfulLoginDate);
        dto.setFirstLoginDate(firstLoginDate);
        dto.setGlobalRole(globalRole);
        dto.setDomainsName(domainsName);
        dto.setDomainRole(RoleDto.valueOf(domainRole.name()));
        dto.setEnabled(enabled);
        return dto;
    }
}
