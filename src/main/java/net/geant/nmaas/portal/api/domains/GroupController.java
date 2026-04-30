package net.geant.nmaas.portal.api.domains;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.applications.ApplicationStatePerDomainDto;
import net.geant.nmaas.api.dto.domains.DomainGroupBaseDto;
import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.api.dto.users.UserViewMinimal;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.portal.api.BaseController;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.exceptions.DataConflictException;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@Slf4j
public class GroupController extends BaseController {

    private static final String ACCESS_DENIED_MESSAGE = "You have no access to this domain group";
    private final DomainService domainService;
    private final DomainGroupService domainGroupService;

    @Autowired
    public GroupController(ModelMapper modelMapper, UserService userService, DomainService domainService, DomainGroupService domainGroupService) {
        super(modelMapper, userService);
        this.domainService = domainService;
        this.domainGroupService = domainGroupService;
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public Id createDomainGroup(@RequestBody DomainGroupDto dto) {
        if (domainGroupService.existDomainGroup(dto.getName(), dto.getCodename())) {
            throw new ProcessingException("Domain group already exists.");
        }
        try {
            DomainGroupDto domainGroupView = domainGroupService.createDomainGroup(dto);
            domainService.updateRolesInDomainGroupByUsers(domainGroupView);
            return new Id(domainGroupView.getId());
        } catch (InvalidDomainException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    @DeleteMapping("/{domainGroupId}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public void deleteDomainGroup(@PathVariable Long domainGroupId) {
        domainGroupService.deleteDomainGroup(domainGroupId);
    }

    @GetMapping
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public List<?> getDomainGroups(
            Principal principal,
            @RequestParam(value = "detailed", required = false, defaultValue = "false") boolean detailed,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        final User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRoles().stream().anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_GROUP_MANAGER))) {
            if (detailed) {
                return domainGroupService.getAllDetailedDomainGroupsWhereManagerIsMember(user, searchValue);
            }
            return domainGroupService.getAllDomainGroupsWhereManagerIsMember(user, searchValue);
        }
        if (detailed) {
            return domainGroupService.getAllDetailedDomainGroups(searchValue);
        }
        return domainGroupService.getAllDomainGroups(searchValue);
    }

    @GetMapping(params = {"page"})
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public Page<DomainGroupBaseDto> getPageDomainGroups(
            Principal principal,
            Pageable pageable,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        final User user = userService.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRoles().stream().anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_GROUP_MANAGER))) {
            return domainGroupService.getPageableAllDomainGroupsWhereManagerIsMemberAndSearch(pageable, user, searchValue);
        }
        return domainGroupService.getPageableAllDomainGroupsAndSearch(pageable, searchValue);
    }

    @GetMapping("/{domainGroupId}")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public DomainGroupDto getDomainGroup(@PathVariable Long domainGroupId,
                                         Principal principal) throws AccessDeniedException {
        DomainGroupDto domainGroup = domainGroupService.getDomainGroup(domainGroupId);
        if (checkManagerPrivileges(principal, domainGroup)) {
            return domainGroup;
        } else {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    @PostMapping("/{domainGroupCodeName}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public DomainGroupDto addDomainsToGroup(@PathVariable String domainGroupCodeName,
                                            @RequestBody List<Long> domainIds) {
        return domainGroupService.addDomainsToGroup(
                domainService.getDomains().stream()
                        .filter(d -> domainIds.contains(d.getId()))
                        .collect(Collectors.toList()),
                domainGroupCodeName);
    }

    @PatchMapping("/{domainGroupId}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public DomainGroupDto deleteDomainFromGroup(@PathVariable Long domainGroupId,
                                                @RequestBody Long domainId) {
        return domainGroupService.deleteDomainFromGroup(
                domainService.findDomain(domainId)
                        .orElseThrow(() -> new IllegalArgumentException(String.format("Domain with id %s doesn't exist", domainId))),
                domainGroupId);
    }

    @PutMapping("/{domainGroupId}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public Id updateDomainGroup(@PathVariable Long domainGroupId,
                                @RequestBody DomainGroupDto domainGroupView,
                                Principal principal) throws AccessDeniedException {
        DomainGroupDto domainGroup = domainGroupService.getDomainGroup(domainGroupId);
        if (checkManagerPrivileges(principal, domainGroup)) {
            domainService.checkDomainGroupUsers(domainGroupView);
            domainService.updateRolesInDomainGroupByUsers(domainGroupView);
            return new Id(domainGroupService.updateDomainGroup(domainGroupId, domainGroupView).getId());
        } else {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    @PutMapping("/{domainGroupId}/members")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public DomainGroupDto updateDomainGroupMembers(@PathVariable Long domainGroupId,
                                                   @RequestBody List<UserViewMinimal> members,
                                                   Principal principal) throws AccessDeniedException {
        DomainGroupDto domainGroup = domainGroupService.getDomainGroup(domainGroupId);
        if (checkManagerPrivileges(principal, domainGroup)) {
            return domainService.updateMembers(members, domainGroup);
        } else {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    @PutMapping("/{domainGroupId}/managers")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public List<UserViewMinimal> addGroupMember(@RequestBody List<Long> userIds,
                                                @PathVariable Long domainGroupId,
                                                Principal principal) throws AccessDeniedException {
        DomainGroupDto domainGroup = domainGroupService.getDomainGroup(domainGroupId);
        if (checkManagerPrivileges(principal, domainGroup)) {
            Set<UserViewMinimal> members = new HashSet<>(domainGroup.getManagers());
            userIds.forEach(userId -> members.add(modelMapper.map(getUser(userId), UserViewMinimal.class)));
            List<UserViewMinimal> userViewMinimals = members.stream()
                    .map(user -> modelMapper.map(user, UserViewMinimal.class))
                    .toList();
            domainService.updateMembers(userViewMinimals, domainGroup);
        } else {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
        return domainService.getMembers(domainGroupId).stream()
                .map(user -> modelMapper.map(user, UserViewMinimal.class))
                .toList();
    }

    @DeleteMapping("/{domainGroupId}/managers")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public List<UserViewMinimal> removeGroupMember(@RequestBody List<Long> userIds,
                                                   @PathVariable Long domainGroupId,
                                                   Principal principal) throws AccessDeniedException {
        DomainGroupDto domainGroup = domainGroupService.getDomainGroup(domainGroupId);
        if (checkManagerPrivileges(principal, domainGroup)) {
            Set<UserViewMinimal> members = new HashSet<>(domainGroup.getManagers());
            userIds.forEach(userId -> members.removeIf(user -> user.getId().equals(userId)));
            List<UserViewMinimal> userViewMinimals = members.stream()
                    .map(user -> modelMapper.map(user, UserViewMinimal.class))
                    .toList();
            domainService.updateMembers(userViewMinimals, domainGroup);
        } else {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
        return domainService.getMembers(domainGroupId).stream()
                .map(user -> modelMapper.map(user, UserViewMinimal.class))
                .toList();
    }

    @PutMapping("/{domainGroupId}/applications")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public List<ApplicationStatePerDomainDto> enableGroupApplication(@PathVariable Long domainGroupId,
                                                                     @RequestBody List<Long> applicationBaseIds,
                                                                     Principal principal) throws AccessDeniedException {
        DomainGroupDto domainGroup = domainGroupService.getDomainGroup(domainGroupId);
        if (checkManagerPrivileges(principal, domainGroup)) {
            domainGroup.getApplicationStatePerDomain().forEach(application ->
                    applicationBaseIds.forEach(applicationBaseId -> {
                        if (application.getApplicationBaseId().equals(applicationBaseId)) {
                            application.setEnabled(true);
                        }
                    }));
            DomainGroupDto result = domainGroupService.updateDomainGroup(domainGroupId, domainGroup);
            return result.getApplicationStatePerDomain();
        } else {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    @DeleteMapping("/{domainGroupId}/applications")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public List<ApplicationStatePerDomainDto> disableGroupApplication(@PathVariable Long domainGroupId,
                                                                      @RequestBody List<Long> applicationBaseIds,
                                                                      Principal principal) throws AccessDeniedException {
        DomainGroupDto domainGroup = domainGroupService.getDomainGroup(domainGroupId);
        if (checkManagerPrivileges(principal, domainGroup)) {
            domainGroup.getApplicationStatePerDomain().forEach(application ->
                    applicationBaseIds.forEach(applicationBaseId -> {
                        if (application.getApplicationBaseId().equals(applicationBaseId)) {
                            application.setEnabled(false);
                        }
                    }));
            DomainGroupDto result = domainGroupService.updateDomainGroup(domainGroupId, domainGroup);
            return result.getApplicationStatePerDomain();
        } else {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    private boolean checkManagerPrivileges(Principal principal, DomainGroupDto domainGroup) {
        return getUser(principal.getName()).getRoles().stream()
                .anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN))
                || domainGroup.getManagers().stream().anyMatch(user ->
                user.getUsername().equalsIgnoreCase(principal.getName())
        );
    }

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public String handleDataConfigException(DataConflictException e) {
        return e.getMessage();
    }

}
