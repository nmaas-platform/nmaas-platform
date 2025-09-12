package net.geant.nmaas.portal.api.market;

import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.exceptions.InvalidDomainException;
import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.exceptions.DataConflictException;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
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
    public Id createDomainGroup(@RequestBody DomainGroupView domainGroup) {
        if (domainGroupService.existDomainGroup(domainGroup.getName(), domainGroup.getCodename())) {
            throw new ProcessingException("Domain group already exists.");
        }
        try {
            DomainGroupView domainGroupView = domainGroupService.createDomainGroup(domainGroup);
            this.domainService.updateRolesInDomainGroupByUsers(domainGroupView);
            return new Id(domainGroupView.getId());
        } catch (InvalidDomainException e) {
            throw new ProcessingException(e.getMessage());
        }
    }

    @DeleteMapping("/{domainGroupId}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public void deleteDomainGroup(@PathVariable Long domainGroupId) {
        this.domainGroupService.deleteDomainGroup(domainGroupId);
    }

    @GetMapping
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public List<DomainGroupView> getDomainGroups(Principal principal) {
        User user = this.userService.findByUsername(principal.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getRoles().stream().anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_GROUP_MANAGER))) {
            return domainGroupService.getAllDomainGroups().stream().filter(group -> group.getManagers().stream()
                    .anyMatch(groupUser -> groupUser.getId().equals(user.getId()))).collect(Collectors.toList());
        }
        return domainGroupService.getAllDomainGroups();
    }

    @GetMapping("/{domainGroupId}")
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public DomainGroupView getDomainGroup(@PathVariable Long domainGroupId, Principal principal) throws AccessDeniedException {
        DomainGroupView domainGroupView = domainGroupService.getDomainGroup(domainGroupId);
        if (getUser(principal.getName()).getRoles().stream().anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN)) ||
                domainGroupView.getManagers().stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(principal.getName()))) {
            return domainGroupView;
        } else {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    @PostMapping("/{domainGroupCodeName}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public DomainGroupView addDomainsToGroup(@PathVariable String domainGroupCodeName,
                                             @RequestBody List<Long> domainIds) {
        return domainGroupService.addDomainsToGroup(
                domainService.getDomains().stream().filter(d -> domainIds.contains(d.getId())).collect(Collectors.toList()),
                domainGroupCodeName);
    }

    @PatchMapping("/{domainGroupId}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public DomainGroupView deleteDomainFromGroup(@PathVariable Long domainGroupId, @RequestBody Long domainId) {
        return domainGroupService.deleteDomainFromGroup(
                domainService.findDomain(domainId).orElseThrow(() -> new IllegalArgumentException(String.format("Domain with id %s doesn't exist", domainId))),
                domainGroupId);
    }

    @PutMapping("/{domainGroupId}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') || hasRole('ROLE_GROUP_MANAGER')")
    public Id updateDomainGroup(@PathVariable Long domainGroupId, @RequestBody DomainGroupView domainGroupView, Principal principal) throws AccessDeniedException {
        DomainGroupView domainGroup = domainGroupService.getDomainGroup(domainGroupId);
        if (getUser(principal.getName()).getRoles().stream().anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN)) ||
                domainGroup.getManagers().stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(principal.getName()))) {
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
    public DomainGroupView updateDomainGroupMembers(@PathVariable Long domainGroupId, @RequestBody List<UserViewMinimal> members, Principal principal) throws AccessDeniedException {
        DomainGroupView domainGroup = domainGroupService.getDomainGroup(domainGroupId);
        if (getUser(principal.getName()).getRoles().stream().anyMatch(userRole -> userRole.getRole().equals(Role.ROLE_SYSTEM_ADMIN)) ||
                domainGroup.getManagers().stream().anyMatch(user -> user.getUsername().equalsIgnoreCase(principal.getName()))) {
            return domainService.updateMembers(members, domainGroup);
        } else {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }
    }

    @ExceptionHandler(DataConflictException.class)
    @ResponseStatus(code = HttpStatus.CONFLICT)
    public String handleDataConfigException(DataConflictException e) {
        return e.getMessage();
    }

}