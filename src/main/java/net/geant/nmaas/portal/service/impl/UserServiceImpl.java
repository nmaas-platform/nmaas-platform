package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.domain.UserListEntry;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.api.exceptions.SignupException;
import net.geant.nmaas.portal.api.security.JWTTokenService;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.persistent.results.UserLoginDate;
import net.geant.nmaas.portal.persistent.spec.UserSpecification;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_DOMAIN_ADMIN;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_SYSTEM_ADMIN;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfigurationManager configurationManager;
    private final ModelMapper modelMapper;

    private final ApplicationEventPublisher eventPublisher;
    private final JWTTokenService jwtTokenService;
    private final DomainGroupService domainGroupService;

    private final UserLoginRegisterService userLoginService;


    @Value("${portal.address}")
    @Setter
    private String portalAddress;

    @Override
    public boolean hasPrivilege(User user, Domain domain, Role role) {
        if (user == null || domain == null || role == null) {
            return false;
        }
        return Objects.nonNull(userRoleRepository.findByDomainAndUserAndRole(domain, user, role));
    }

    @Override
    public boolean canUpdateData(String username, final List<UserRole> userRoles) {
        checkParam(username);
        User user = findByUsername(username).orElseThrow(() -> new MissingElementException("User with username " + username + " not found"));
        return isAdmin(user) || isDomainAdminInUserDomain(user, userRoles);
    }

    private boolean isDomainAdminInUserDomain(User admin, final List<UserRole> userRoles) {
        return admin.getRoles().stream()
                .filter(role -> role.getRole().equals(ROLE_DOMAIN_ADMIN))
                .anyMatch(role -> userRoles.stream().anyMatch(userRole -> userRole.getDomain().equals(role.getDomain())));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRole().equals(ROLE_SYSTEM_ADMIN));
    }

    @Override
    public boolean isAdmin(String username) {
        User user = findByUsername(username).orElseThrow(() -> new MissingElementException("User with username " + username + " not found"));
        return user.getRoles().stream().anyMatch(role -> role.getRole().equals(ROLE_SYSTEM_ADMIN));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return (username != null ? userRepository.findByUsername(username) : Optional.empty());
    }

    @Override
    public Optional<User> findById(Long id) {
        return (id != null ? userRepository.findById(id) : Optional.empty());
    }

    @Override
    public Optional<User> findBySamlToken(String token) {
        return (token != null ? userRepository.findBySamlToken(token) : Optional.empty());
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User with mail " + email + " not found"));
    }

    @Override
    public boolean existsByUsername(String username) {
        checkParam(username);
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsById(Long id) {
        checkParam(id);
        return userRepository.existsById(id);
    }

    @Override
    public boolean existsBySamlToken(String token) {
        checkParam(token);
        return userRepository.existsBySamlToken(token);
    }

    @Override
    @Transactional
    public User register(Registration registration, Domain globalDomain, Domain domain) {
        if (userRepository.existsByUsername(registration.getUsername()) || userRepository.existsByEmail(registration.getEmail())) {
            throw new SignupException("User already exists");
        }
        User newUser = new User(registration.getUsername(), false, passwordEncoder.encode(registration.getPassword()), globalDomain, Role.ROLE_GUEST);
        newUser.setEmail(registration.getEmail());
        newUser.setFirstname(registration.getFirstname());
        newUser.setLastname(registration.getLastname());
        newUser.setEnabled(false);
        if (domain != null) {
            newUser.setNewRoles(Set.of(new UserRole(newUser, domain, Role.ROLE_GUEST)));
        }
        newUser.setTermsOfUseAccepted(registration.getTermsOfUseAccepted());
        newUser.setPrivacyPolicyAccepted(registration.getPrivacyPolicyAccepted());
        newUser.setSelectedLanguage(configurationManager.getConfiguration().getDefaultLanguage());
        userRepository.save(newUser);
        return newUser;
    }


    @Override
    public User registerBulk(CsvDomain csvUser, Domain globalDomain, Domain domain) {
        if (userRepository.existsByUsername(csvUser.getAdminUserName()) || userRepository.existsByEmail(csvUser.getEmail())) {
            throw new SignupException("User already exists");
        }
        String temporaryPassword = RandomStringUtils.random(16);
        log.info("Creating user {} with temporary password", csvUser.getAdminUserName());
        User newUser = new User(csvUser.getAdminUserName(), false, passwordEncoder.encode(temporaryPassword), globalDomain, Role.ROLE_GUEST);
        newUser.setEmail(csvUser.getEmail());
        newUser.setEnabled(true);
        newUser.setSelectedLanguage(configurationManager.getConfiguration().getDefaultLanguage());
        newUser.setTermsOfUseAccepted(true);
        newUser.setPrivacyPolicyAccepted(true);
        newUser.setFirstname(csvUser.getAdminUserName());
        newUser.setLastname(csvUser.getAdminUserName());
        if (domain != null) {
            newUser.setNewRoles(Set.of(new UserRole(newUser, domain, ROLE_DOMAIN_ADMIN)));
        }
        boolean sendMails = configurationManager.getConfiguration().isBulkDomainsSendEmailForNewAccounts();
        // set user saml_token to email address if a sso account requested
        if (configurationManager.getConfiguration().isBulkDomainsAllowForSsoAccounts()) {
            if (csvUser.getSsoEnabled() != null && csvUser.getSsoEnabled()) {
                newUser.setSamlToken(csvUser.getEmail());
                if (sendMails) this.sendMail(newUser, MailType.NEW_BULK_SSO_LOGIN);
            } else {
                if (sendMails) this.sendMail(newUser, MailType.NEW_BULK_LOGIN);
            }
        } else {
            if (sendMails) this.sendMail(newUser, MailType.NEW_BULK_LOGIN);
        }
        userRepository.save(newUser);
        return newUser;
    }

    @Override
    public void update(User user) {
        checkParam(user);
        checkParam(user.getId());
        if (!userRepository.existsById(user.getId())) {
            throw new ProcessingException("User with id " + user.getId() + " does not exist");
        }
        userRepository.saveAndFlush(user);
    }

    @Override
    public void delete(User user) {
        checkParam(user);
        checkParam(user.getId());
        domainGroupService.deleteUserFromAllDomainsGroups(user);
        userRepository.delete(user);
    }

    @Override
    public void deleteById(Long userId) {
        checkParam(userId);
        domainGroupService.deleteUserFromAllDomainsGroups(userRepository.getReferenceById(userId));
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public void setEnabledFlag(Long userId, boolean isEnabled) {
        userRepository.setEnabledFlag(userId, isEnabled);
    }

    @Override
    @Transactional
    public void setUserLanguage(Long userId, final String userLanguage) {
        userRepository.setUserLanguage(userId, userLanguage);
    }

    @Override
    @Transactional
    public void setUserTheme(Long userId, String defaultTheme) {
        userRepository.setUserThemeMode(userId, defaultTheme);
    }

    @Override
    @Transactional
    public void setTermsOfUseAcceptedFlag(Long userId, boolean termsOfUseAcceptedFlag) {
        userRepository.setTermsOfUseAcceptedFlag(userId, termsOfUseAcceptedFlag);
    }

    @Override
    @Transactional
    public void setTermsOfUseAcceptedFlagByUsername(String username, boolean termsOfUseAcceptedFlag) {
        User user = userRepository.findByUsername(username).orElseThrow(()
                -> new UsernameNotFoundException("User " + username + " not found."));
        userRepository.setTermsOfUseAcceptedFlag(user.getId(), termsOfUseAcceptedFlag);
    }

    @Override
    @Transactional
    public void setPrivacyPolicyAcceptedFlag(Long userId, boolean privacyPolicyAcceptedFlag) {
        userRepository.setPrivacyPolicyAcceptedFlag(userId, privacyPolicyAcceptedFlag);
    }

    @Override
    @Transactional
    public void setPrivacyPolicyAcceptedFlagByUsername(String username, boolean privacyPolicyAcceptedFlag) {
        User user = userRepository.findByUsername(username).orElseThrow(()
                -> new UsernameNotFoundException("User " + username + " not found."));
        userRepository.setPrivacyPolicyAcceptedFlag(user.getId(), privacyPolicyAcceptedFlag);
    }

    private void checkParam(Long id) {
        if (id == null)
            throw new IllegalArgumentException("id is null");
    }

    private void checkParam(String username) {
        if (username == null)
            throw new IllegalArgumentException("username is null");
    }

    private void checkParam(User user) {
        if (user == null)
            throw new IllegalArgumentException("user is null");
    }

    private void checkParamSaml(String samlToken) {
        if (samlToken == null)
            throw new IllegalArgumentException("samlToken is null");
    }

    @Override
    @Transactional
    public List<UserView> findAllUsersWithAdminRole() {
        return findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getRole().name().equalsIgnoreCase(Role.ROLE_SYSTEM_ADMIN.name())))
                .map(user -> modelMapper.map(user, UserView.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<UserView> findUsersWithRoleSystemAdminAndOperator() {
        return findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> role.getRole().name().equalsIgnoreCase(Role.ROLE_SYSTEM_ADMIN.name()) || role.getRole().name().equalsIgnoreCase(Role.ROLE_OPERATOR.name())))
                .map(user -> modelMapper.map(user, UserView.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isUserAdminInAnyDomainById(List<Long> domainIds, String username) {
        Boolean result = false;
        for (Long domainId : domainIds) {
            if (userRoleRepository.findRolesByDomainAndUser(domainId, username).contains(ROLE_DOMAIN_ADMIN)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean isUserAdminInAnyDomain(List<Domain> domains, String username) {
        Boolean result = false;
        for (Domain domain : domains) {
            if (userRoleRepository.findRolesByDomainAndUser(domain.getId(), username).contains(ROLE_DOMAIN_ADMIN)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public Page<UserListEntry> findAllListEntry(Pageable pageable, String searchValue) {
        Map<Long, UserLoginDate> userLoginDateMap = this.userLoginService.getAllFirstAndLastSuccessfulLoginDate().stream()
                .map(x -> new AbstractMap.SimpleEntry<>(x.getUserId(), x))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (searchValue != null && !searchValue.isEmpty()) {
            Specification<User> searchSpec = UserSpecification.findBySearchValue(searchValue);
            Page<User> all = userRepository.findAll(searchSpec, pageable);
            return all.map(this::toListView).map(u -> mapUser(u, userLoginDateMap));
        } else {
            return userRepository.findAllListEntry(pageable).map(u -> mapUser(u, userLoginDateMap));
        }
    }

    @Override
    public Page<UserListEntry> findAllInDomainListEntry(Long domainId, Pageable pageable, String searchValue) {
        Map<Long, UserLoginDate> userLoginDateMap = this.userLoginService.getAllFirstAndLastSuccessfulLoginDate().stream()
                .map(x -> new AbstractMap.SimpleEntry<>(x.getUserId(), x))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (searchValue != null && !searchValue.isEmpty()) {
            Specification<User> searchSpec = UserSpecification.findBySearchValue(searchValue)
                    .and(UserSpecification.findByDomain(domainId));
            Page<User> all = userRepository.findAll(searchSpec, pageable);
            return all.map(this::toListView).map(u -> mapUser(u, userLoginDateMap));
        } else {
            return userRepository.findAllInDomainListEntry(domainId, pageable).map(u -> mapUser(u, userLoginDateMap));
        }
    }

    @Override
    public Optional<Role> getUserRoleInDomain(Long userId, Long domainId) {
        return this.userRoleRepository.findRolesByDomainAndUser(domainId, userId).stream().findFirst();
    }

    private void sendMail(User user, MailType mailType) {
        Map<String, Object> map;
        if (mailType == MailType.NEW_BULK_LOGIN) {
            map = Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "accessURL", generateResetPasswordUrl(this.jwtTokenService.getResetToken24Hours(user.getEmail()))
            );
        } else {
            map = Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "portal", this.portalAddress);
        }
        MailAttributes mailAttributes = MailAttributes.builder()
                .otherAttributes(map)
                .mailType(mailType)
                .build();
        eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
    }

    private String generateResetPasswordUrl(String token) {
        String url = this.portalAddress;
        if (url == null) {
            return "reset/" + token;
        }
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + "reset/" + token;
    }

    private UserListEntry mapUser(UserListEntry entry, final Map<Long, UserLoginDate> userLoginDateMap) {
        if (userLoginDateMap.containsKey(entry.getId())) {
            entry.setLastSuccessfulLoginDate(userLoginDateMap.get(entry.getId()).getMaxLoginDate());
            entry.setFirstLoginDate(userLoginDateMap.get(entry.getId()).getMinLoginDate());
        }
        return entry;
    }

    private UserListEntry toListView(User user) {
        return new UserListEntry(user);
    }
}
