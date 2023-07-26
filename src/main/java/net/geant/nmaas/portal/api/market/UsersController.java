package net.geant.nmaas.portal.api.market;

import com.google.common.collect.ImmutableMap;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.domain.PasswordChange;
import net.geant.nmaas.portal.api.domain.PasswordReset;
import net.geant.nmaas.portal.api.domain.UserBase;
import net.geant.nmaas.portal.api.domain.UserRequest;
import net.geant.nmaas.portal.api.domain.UserRoleView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.exceptions.ObjectNotFoundException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.results.UserLoginDate;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import net.geant.nmaas.utils.captcha.ValidateCaptcha;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import java.security.Principal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_DOMAIN_ADMIN;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_GUEST;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_OPERATOR;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_SYSTEM_ADMIN;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_TOOL_MANAGER;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_USER;

@RestController
@RequestMapping("/api")
@Log4j2
public class UsersController {

    private static final String USER_NOT_FOUND_ERROR_MESSAGE = "User not found.";
    private static final String DOMAIN_NOT_FOUND_ERROR_MESSAGE = "Domain not found.";
    private static final String ROLE_CANNOT_BE_ASSIGNED_ERROR_MESSAGE = "Role cannot be assigned.";

    @Value("${portal.address}")
    private String portalAddress;

    private final UserService userService;

    private final DomainService domainService;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    private final JWTTokenService jwtTokenService;

    private final ApplicationEventPublisher eventPublisher;

    private final UserLoginRegisterService userLoginService;

    private final ApplicationInstanceService instanceService;

    @Autowired
    public UsersController(UserService userService,
                           DomainService domainService,
                           ModelMapper modelMapper,
                           PasswordEncoder passwordEncoder,
                           JWTTokenService jwtTokenService,
                           ApplicationEventPublisher eventPublisher,
                           UserLoginRegisterService userLoginService,
                           ApplicationInstanceService instanceService) {
        this.userService = userService;
        this.domainService = domainService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.eventPublisher = eventPublisher;
        this.userLoginService = userLoginService;
        this.instanceService = instanceService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') or hasRole('ROLE_DOMAIN_ADMIN')")
    @Transactional
    public List<UserBase> getUsers(Pageable pageable, Principal principal) {

        User owner = this.userService.findByUsername(principal.getName()).orElseThrow(
                () -> new RuntimeException("User with username: " + principal.getName() + " does not exist"));
        /* when user is not system admin, then return only basic information about users */
        if (owner.getRoles().stream().noneMatch(role -> role.getRole() == ROLE_SYSTEM_ADMIN)) {
            return userService.findAll(pageable).getContent().stream()
                    .filter(user -> user.getRoles().stream().anyMatch( // select only users with guest role in global domain
                            role -> role.getRole() == ROLE_GUEST &&
                                    role.getDomain().getId().equals(
                                            domainService.getGlobalDomain().orElseThrow(
                                                    () -> new RuntimeException("Global domain does not exist")).getId()))
                    )
                    .map(user -> modelMapper.map(user, UserViewMinimal.class)).collect(Collectors.toList());
        }

        /* reads all users first and last successful login, transforms it to map*/
        Map<Long, UserLoginDate> userLoginDateMap = this.userLoginService.getAllFirstAndLastSuccessfulLoginDate().stream()
                .map(x -> new AbstractMap.SimpleEntry<>(x.getUserId(), x))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        /* updates user view with first and last login date */
        return userService.findAll(pageable).getContent().stream()
                .map(user -> mapUser(user, userLoginDateMap))
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/users/roles")
    public List<Role> getRoles() {
        return Arrays.stream(Role.values())
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/users/{userId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') or hasRole('ROLE_DOMAIN_ADMIN')")
    public UserBase retrieveUser(@PathVariable("userId") Long userId, Principal principal) {
        User user = getUser(userId);
        User owner = this.userService.findByUsername(principal.getName()).orElseThrow(
                () -> new RuntimeException("User with username: " + principal.getName() + " does not exist"));
        /* calculate set of common domains between principal and user */
        Set<Long> common = user.getRoles().stream().map(role -> role.getDomain().getId()).collect(Collectors.toSet());
        common.retainAll(owner.getRoles().stream().map(role -> role.getDomain().getId()).collect(Collectors.toSet()));
        /* if no common domain return only basic set of user information */
        if (common.isEmpty()) {
            return modelMapper.map(user, UserViewMinimal.class);
        }
        UserView uv = modelMapper.map(user, UserView.class);
        /* updates user view with first and last login date */
        userLoginService.getUserFirstAndLastSuccessfulLoginDate(user).ifPresent(userLoginDate -> {
            uv.setFirstLoginDate(userLoginDate.getMinLoginDate());
            uv.setLastSuccessfulLoginDate(userLoginDate.getMaxLoginDate());
        });
        return uv;
    }

    @PutMapping(value = "/users/{userId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void updateUser(@PathVariable("userId") final Long userId, @RequestBody final UserRequest userRequest, final Principal principal) {
        User userDetails = userService.findById(userId).orElseThrow(() -> new MissingElementException(USER_NOT_FOUND_ERROR_MESSAGE));

        if (userRequest == null) {
            throw new MissingElementException("User request is null");
        }
        if (!userDetails.getUsername().equals(principal.getName()) && !userService.canUpdateData(principal.getName(), userDetails.getRoles())) {
            throw new ProcessingException(principal.getName() + " was trying to edit data of user " + userDetails.getUsername() + " without required role.");
        }
        String message = getMessageWhenUserUpdated(userDetails, userRequest);
        final String userRoles = getRoleAsString(userDetails.getRoles());
        if (userRequest.getFirstname() != null) {
            userDetails.setFirstname(userRequest.getFirstname());
        }
        if (userRequest.getLastname() != null) {
            userDetails.setLastname(userRequest.getLastname());
        }
        if (userRequest.getEmail() != null && !userRequest.getEmail().equalsIgnoreCase(userDetails.getEmail())) {
            if (userService.existsByEmail(userRequest.getEmail())) {
                throw new ProcessingException("User with mail " + userRequest.getEmail() + " already exists.");
            }
            userDetails.setEmail(userRequest.getEmail());
        }
        userDetails.setDefaultDomain(userRequest.getDefaultDomain());
        userService.update(userDetails);
        if (!StringUtils.isEmpty(message)) {
            log.info(String.format("Data of user [%s] with role [%s] were updated. The following changes are: [%s] ",
                    userDetails.getUsername(),
                    userRoles,
                    message));
        }
    }

    @DeleteMapping(value = "/users/{userId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteUser(@PathVariable("userId") Long userId) {
        User user = this.userService.findById(userId).orElseThrow(() -> new MissingElementException("User not found"));
        Long globalDomainId = this.domainService.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found")).getId();
        if (user.getRoles().stream().anyMatch(userRole -> userRole.getRole().equals(ROLE_SYSTEM_ADMIN))) {
            throw new ProcessingException("Cannot delete SYSTEM ADMIN");
        }
        List<UserRole> notGuestInGlobalDomainRole = user.getRoles().stream()
                .filter(userRole ->
                        !(userRole.getRole().equals(ROLE_GUEST) && userRole.getDomain().getId().equals(globalDomainId))
                )
                .collect(Collectors.toList());
        if (notGuestInGlobalDomainRole.size() > 0) {
            throw new ProcessingException("User cannot be deleted because he belongs to at least one domain");
        }
        if (!this.instanceService.findAllByOwner(userId).isEmpty()) {
            throw new ProcessingException("User cannot be deleted because he had deployed at least one instance");
        }
        this.userService.deleteById(userId);
    }

    @GetMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') or hasRole('ROLE_DOMAIN_ADMIN')")
    public Set<UserRoleView> getUserRoles(@PathVariable Long userId) {
        User user = getUser(userId);
        return user.getRoles().stream().map(ur -> modelMapper.map(ur, UserRoleView.class)).collect(Collectors.toSet());
    }

    @DeleteMapping("/users/{userId}/roles")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public void removeUserRole(@PathVariable final Long userId,
                               @RequestBody final UserRoleView userRole,
                               final Principal principal) {
        if (userRole == null) {
            throw new MissingElementException("userRole is null");
        }

        if (userRole.getRole() == null) {
            throw new MissingElementException("Missing role");
        }

        Domain domain;
        if (userRole.getDomainId() == null) {
            domain = domainService.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found"));
        } else {
            domain = domainService.findDomain(userRole.getDomainId()).orElseThrow(() -> new MissingElementException(DOMAIN_NOT_FOUND_ERROR_MESSAGE));
        }

        User user = getUser(userId);

        try {
            domainService.removeMemberRole(domain.getId(), user.getId(), userRole.getRole());
            final User adminUser = userService.findByUsername(principal.getName()).orElseThrow(() -> new ObjectNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
            final String adminRoles = getRoleAsString(adminUser.getRoles());
            log.info(String.format("User [%s] with role [%s] removed role [%s] from user [%s] in domain [%d]",
                    principal.getName(),
                    adminRoles,
                    userRole.getRole().authority(),
                    user.getUsername(),
                    userRole.getDomainId()));
            domainService.addGlobalGuestUserRoleIfMissing(userId);
        } catch (ObjectNotFoundException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    @PostMapping(value = "/users/terms/{username}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ROLE_NOT_ACCEPTED')")
    public void setAcceptance(@PathVariable String username) {
        try {
            setAcceptanceFlags(username);
            log.info(String.format("User [%s] accepted Terms of Use and Privacy Policy", username));
        } catch (ProcessingException err) {
            throw new MissingElementException(err.getMessage());
        }
    }

    private void setAcceptanceFlags(String username) {
        try {
            userService.setTermsOfUseAcceptedFlagByUsername(username, true);
            userService.setPrivacyPolicyAcceptedFlagByUsername(username, true);
        } catch (UsernameNotFoundException err) {
            throw new ProcessingException(err.getMessage());
        }
    }

    @PostMapping(value = "/users/complete")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ROLE_INCOMPLETE')")
    @Transactional
    public void completeRegistration(Principal principal, @RequestBody UserRequest userRequest) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new MissingElementException("Internal error. User not found."));
        Long domainId = domainService.getGlobalDomain().orElseThrow(ProcessingException::new).getId();
        completeRegistration(userRequest, user, domainId);
    }

    private void completeRegistration(UserRequest userRequest, User user, Long domainId) {
        if (userService.existsByUsername(userRequest.getUsername())) {
            throw new ProcessingException("User with same username already exists");
        } else {
            user.setUsername(userRequest.getUsername());
        }
        if (userRequest.getFirstname() != null) {
            user.setFirstname(userRequest.getFirstname());
        }
        if (userRequest.getLastname() != null) {
            user.setLastname(userRequest.getLastname());
        }
        if (userRequest.getEmail() != null) {
            if (userService.existsByEmail(userRequest.getEmail())) {
                throw new ProcessingException("User with mail " + userRequest.getEmail() + " already exists");
            }
            user.setEmail(userRequest.getEmail());
        }

        domainService.addMemberRole(domainId, user.getId(), Role.ROLE_GUEST);
        domainService.addGlobalGuestUserRoleIfMissing(user.getId());
        userService.update(user);
        this.sendMail(this.userService.findAllUsersWithAdminRole().get(0), MailType.NEW_SSO_LOGIN, ImmutableMap.of("newUser", user.getUsername()));
    }

    @PostMapping("/users/reset/notification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendResetPasswordNotification(@RequestBody String email) {
        User user = userService.findByEmail(email);
        checkSSOUser(user);
        this.sendMail(modelMapper.map(user, UserView.class), MailType.PASSWORD_RESET, ImmutableMap.of("accessURL", generateResetPasswordUrl(this.jwtTokenService.getResetToken(email))));
    }

    private String generateResetPasswordUrl(String token) {
        String url = this.portalAddress;
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + "reset/" + token;
    }

    @PostMapping("/users/reset/validate")
    @ResponseStatus(HttpStatus.OK)
    public UserView validateResetRequest(@RequestBody String token) {
        try {
            Claims claims = jwtTokenService.getResetClaims(token);
            User user = userService.findByEmail(claims.getSubject());
            checkSSOUser(user);
            return modelMapper.map(user, UserView.class);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ProcessingException("Validation of reset request failed -> " + e.getMessage());
        }
    }

    @PostMapping("/users/reset")
    @ValidateCaptcha
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void resetPassword(@RequestBody PasswordReset passwordReset, @RequestParam String token) {
        try {
            Claims claims = jwtTokenService.getResetClaims(passwordReset.getToken());
            User user = userService.findByEmail(claims.getSubject());
            checkSSOUser(user);
            changePassword(user, passwordReset.getPassword());
        } catch (JwtException | IllegalArgumentException e) {
            throw new ProcessingException("Unable to reset password -> " + e.getMessage());
        }
    }

    @PostMapping("/users/my/auth/basic/password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void changePassword(Principal principal, @RequestBody PasswordChange passwordChange) {
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new ProcessingException("Internal error. User not found."));
        checkSSOUser(user);
        checkPassword(user, passwordChange.getPassword());
        changePassword(user, passwordChange.getNewPassword());
    }

    private void checkPassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new ProcessingException("Password mismatch");
    }

    private void changePassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        userService.update(user);
    }

    private void checkSSOUser(User user) {
        if (StringUtils.isNotEmpty(user.getSamlToken())) {
            throw new ProcessingException("SSO user cannot change or reset password");
        }
    }

    @GetMapping("/domains/{domainId}/users")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    public List<UserViewMinimal> getDomainUsers(@PathVariable Long domainId) {
        return domainService.getMembers(domainId).stream()
                .map(this::mapMinimalUser)
                .collect(Collectors.toList());
    }

    @GetMapping("/domains/{domainId}/users/admin")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public List<UserView> getDomainUsersAsAdmin(@PathVariable Long domainId) {
        /* reads all users first and last successful login, transforms it to map*/
        Map<Long, UserLoginDate> userLoginDateMap = this.userLoginService.getAllFirstAndLastSuccessfulLoginDate().stream()
                .map(x -> new AbstractMap.SimpleEntry<>(x.getUserId(), x))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return domainService.getMembers(domainId).stream()
                .map(member -> mapUser(member, userLoginDateMap))
                .collect(Collectors.toList());
    }

    @GetMapping("/domains/{domainId}/users/{userId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    public UserView getDomainUser(@PathVariable Long domainId, @PathVariable Long userId) {
        try {
            User user = domainService.getMember(domainId, userId);
            UserView uv = modelMapper.map(user, UserView.class);
            userLoginService.getUserFirstAndLastSuccessfulLoginDate(user).ifPresent(userLoginDate -> {
                uv.setFirstLoginDate(userLoginDate.getMinLoginDate());
                uv.setLastSuccessfulLoginDate(userLoginDate.getMaxLoginDate());
            });
            return uv;
        } catch (ObjectNotFoundException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    @DeleteMapping("/domains/{domainId}/users/{userId}")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void removeDomainUser(@PathVariable Long domainId, @PathVariable Long userId) {
        Domain domain = getDomain(domainId);

        User user = getUser(userId);

        try {
            domainService.removeMember(domain.getId(), user.getId());
        } catch (ObjectNotFoundException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    @GetMapping("/domains/{domainId}/users/{userId}/roles")
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    public Set<Role> getUserRoles(@PathVariable Long domainId, @PathVariable Long userId) {
        Domain domain = getDomain(domainId);

        User user = getUser(userId);

        try {
            return domainService.getMemberRoles(domain.getId(), user.getId());
        } catch (ObjectNotFoundException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    @PostMapping("/domains/{domainId}/users/{userId}/roles")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public void addUserRole(@PathVariable final Long domainId,
                            @PathVariable final Long userId,
                            @RequestBody final UserRoleView userRole,
                            final Principal principal) {

        if (userRole == null) {
            throw new MissingElementException("Empty request");
        }
        if (userRole.getRole() == null) {
            throw new MissingElementException("Missing role");
        }
        Role role = userRole.getRole();

        if (!domainId.equals(userRole.getDomainId())) {
            throw new ProcessingException("Invalid request domain");
        }

        final Domain domain = getDomain(domainId);
        final Domain globalDomain = domainService.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found"));

        if (domain.equals(globalDomain)) {
            if ((Stream.of(ROLE_SYSTEM_ADMIN, ROLE_TOOL_MANAGER, ROLE_OPERATOR, ROLE_GUEST).noneMatch(allowed -> allowed == role))) {
                throw new ProcessingException(ROLE_CANNOT_BE_ASSIGNED_ERROR_MESSAGE);
            }
        } else {
            if (Stream.of(ROLE_GUEST, ROLE_USER, ROLE_DOMAIN_ADMIN).noneMatch(allowed -> allowed == role)) {
                throw new ProcessingException(ROLE_CANNOT_BE_ASSIGNED_ERROR_MESSAGE);
            }
        }

        final User user = getUser(userId);

        try {
            domainService.addMemberRole(domain.getId(), user.getId(), role);

            final User adminUser = userService.findByUsername(principal.getName()).orElseThrow(() -> new ObjectNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
            final String adminRoles = getRoleAsString(adminUser.getRoles());

            log.info(String.format("User [%s] with role [%s] added role [%s] to user [%s] in domain [%d].",
                    principal.getName(),
                    adminRoles,
                    userRole.getRole().authority(),
                    user.getUsername(),
                    domain.getId()
            ));
        } catch (ObjectNotFoundException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    @DeleteMapping("/domains/{domainId}/users/{userId}/roles/{userRole}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasPermission(#domainId, 'domain', 'OWNER')")
    @Transactional
    public void removeUserRole(@PathVariable final long domainId,
                               @PathVariable final Long userId,
                               @PathVariable final String userRole,
                               final Principal principal) {
        final Role role = convertRole(userRole);

        final Domain domain = getDomain(domainId);
        final User user = getUser(userId);

        try {
            domainService.removeMemberRole(domain.getId(), user.getId(), role);
            final User adminUser = userService.findByUsername(principal.getName()).orElseThrow(() -> new ObjectNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
            final String adminRoles = getRoleAsString(adminUser.getRoles());

            log.info(String.format("User [%s] with role [%s] removed role [%s] of user name [%s] in domain [%d].",
                    principal.getName(),
                    adminRoles,
                    role.authority(),
                    user.getUsername(),
                    domainId)
            );
            domainService.addGlobalGuestUserRoleIfMissing(userId);
        } catch (ObjectNotFoundException e) {
            throw new MissingElementException(e.getMessage());
        }
    }

    @PutMapping("/users/status/{userId}")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void setEnabledFlag(@PathVariable Long userId,
                               @RequestParam("enabled") final boolean isEnabledFlag,
                               final Principal principal) {
        try {
            userService.setEnabledFlag(userId, isEnabledFlag);
            User user = userService.findById(userId).orElseThrow(() -> new MissingElementException(USER_NOT_FOUND_ERROR_MESSAGE));
            User adminUser = userService.findByUsername(principal.getName()).orElseThrow(() -> new ObjectNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
            List<Role> rolesList = adminUser.getRoles().stream().map(UserRole::getRole).collect(Collectors.toList());
            List<String> rolesAsStringList = rolesList.stream().map(Role::authority).collect(Collectors.toList());
            String roleAsString = String.join(",", rolesAsStringList);
            String message = String.format("User [%s] with role [%s] [%s] account of user [%s].",
                    principal.getName(),
                    roleAsString,
                    isEnabledFlag ? "activated" : "deactivated",
                    getUser(userId).getUsername());
            if (isEnabledFlag) {
                this.sendMail(modelMapper.map(user, UserView.class), MailType.ACCOUNT_ACTIVATED, ImmutableMap.of("portalURL", portalAddress != null ? portalAddress : ""));
            } else {
                this.sendMail(modelMapper.map(user, UserView.class), MailType.ACCOUNT_BLOCKED, Collections.emptyMap());
            }
            log.info(message);
        } catch (ObjectNotFoundException err) {
            throw new MissingElementException(err.getMessage());
        }
    }

    @PatchMapping("/users/{userId}/language")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void setDefaultLanguage(@PathVariable Long userId, @RequestParam("defaultLanguage") final String defaultLanguage) {
        this.userService.setUserLanguage(userId, defaultLanguage);
    }

    @GetMapping(value = "/users/search", params = {"searchPart", "domainId"})
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN') or hasRole('ROLE_DOMAIN_ADMIN')")
    public List<UserViewMinimal> searchUser(@RequestParam(required = false) String searchPart, @RequestParam(required = false) Long domainId) {
        List<UserViewMinimal> result = new ArrayList<>();
        String search = searchPart.toLowerCase();

        List<User> allUsers = this.userService.findAll().stream()
                .filter(User::isEnabled)
                .filter(user -> Objects.nonNull(user.getEmail()))
                .collect(Collectors.toList());
        result = allUsers.stream().
                filter(user -> user.getEmail().toLowerCase().contentEquals(search))
                .filter(user -> user.getRoles().stream().noneMatch(roles -> roles.getDomain().getId().equals(domainId)))
                .map(this::mapMinimalUser).collect(Collectors.toList());
        return result;
    }

    private Role convertRole(String userRole) {
        Role role;
        try {
            role = Role.valueOf(userRole);
        } catch (IllegalArgumentException ex) {
            throw new MissingElementException("Missing or invalid role");
        }
        return role;
    }

    Domain getDomain(Long domainId) {
        return domainService.findDomain(domainId).orElseThrow(() -> new MissingElementException(DOMAIN_NOT_FOUND_ERROR_MESSAGE));
    }

    User getUser(Long userId) {
        return userService.findById(userId).orElseThrow(() -> new MissingElementException(USER_NOT_FOUND_ERROR_MESSAGE));
    }

    String getRoleAsString(List<UserRole> userRoles) {
        return String.join(", ", getRoleAsList(userRoles));
    }

    List<String> getRoleAsList(List<UserRole> userRoles) {
        final List<Role> rolesList = userRoles.stream().map(UserRole::getRole).collect(Collectors.toList());
        return rolesList.stream().map(Role::authority).collect(Collectors.toList());
    }

    List<String> getRequestedRoleAsList(Set<UserRoleView> userRoles) {
        final List<Role> rolesList = userRoles.stream().map(UserRoleView::getRole).collect(Collectors.toList());
        return rolesList.stream().map(Role::authority).collect(Collectors.toList());
    }

    String getRoleWithDomainIdAsString(Set<UserRoleView> userRoles) {
        return userRoles.stream().map(x -> x.getRole().authority() + "@domain" + x.getDomainId())
                .collect(Collectors.joining(", "));
    }

    String getMessageWhenUserUpdated(final User user, final UserRequest userRequest) {
        final String mid = "] -> [";
        StringBuilder message = new StringBuilder();
        if (!isSame(userRequest.getUsername(), user.getUsername())) {
            message.append(" Username [").append(user.getUsername()).append(mid).append(userRequest.getUsername()).append("]");
        }
        if (!isSame(userRequest.getEmail(), user.getEmail())) {
            message.append(" Email [").append(user.getEmail()).append(mid).append(userRequest.getEmail()).append("]");
        }
        if (!isSame(userRequest.getFirstname(), user.getFirstname())) {
            message.append(" First name [").append(user.getFirstname()).append(mid).append(userRequest.getFirstname()).append("]");
        }
        if (!isSame(userRequest.getLastname(), user.getLastname())) {
            message.append(" Last name [").append(user.getLastname()).append(mid).append(userRequest.getLastname()).append("]");
        }
        if (!userRequest.isEnabled() == user.isEnabled()) {
            message.append(" Enabled flag [").append(user.isEnabled()).append(mid).append(userRequest.isEnabled()).append("]");
        }
        if (!isSame(getRequestedRoleAsList(userRequest.getRoles()), getRoleAsList(user.getRoles()))) {
            message.append(" Roles changed [").append(getRoleAsString(user.getRoles())).append(mid).append(getRoleWithDomainIdAsString(userRequest.getRoles())).append("]");
        }
        return message.toString();
    }

    private boolean isSame(String newDetail, String oldDetail) {
        newDetail = StringUtils.isEmpty(newDetail) ? "" : newDetail;
        oldDetail = StringUtils.isEmpty(oldDetail) ? "" : oldDetail;
        return newDetail.equalsIgnoreCase(oldDetail);
    }

    private boolean isSame(List<String> requestRoleList, List<String> userRoleList) {
        return requestRoleList.containsAll(userRoleList) && userRoleList.containsAll(requestRoleList);
    }

    private void sendMail(UserView user, MailType mailType, Map<String, Object> other) {
        MailAttributes mailAttributes = MailAttributes.builder()
                .mailType(mailType)
                .otherAttributes(other)
                .addressees(Collections.singletonList(modelMapper.map(user, UserView.class)))
                .build();
        this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
    }

    private UserView mapUser(User user, final Map<Long, UserLoginDate> userLoginDateMap) {
        UserView uv = modelMapper.map(user, UserView.class);
        if (userLoginDateMap.containsKey(uv.getId())) {
            uv.setLastSuccessfulLoginDate(userLoginDateMap.get(uv.getId()).getMaxLoginDate());
            uv.setFirstLoginDate(userLoginDateMap.get(uv.getId()).getMinLoginDate());
        }
        return uv;
    }

    private UserViewMinimal mapMinimalUser(User user) {
        UserViewMinimal userViewMinimal = modelMapper.map(user, UserViewMinimal.class);
        userViewMinimal.setHaveSsh(!user.getSshKeys().isEmpty());
        return userViewMinimal;
    }

}

