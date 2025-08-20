package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.domain.UserListEntry;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    boolean hasPrivilege(User user, Domain domain, Role role);

    boolean canUpdateData(String username, List<UserRole> userRoles);

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    Optional<User> findBySamlToken(String token);

    User findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsById(Long id);

    boolean existsBySamlToken(String token);

    User register(Registration registration, Domain globalDomain, Domain domain);

    User registerBulk(CsvDomain userCSV, Domain globalDomain, Domain domain);

    List<User> findAll();

    Page<User> findAll(Pageable pageable);

    void delete(User user);

    void deleteById(Long userId);

    void update(User user);

    void setEnabledFlag(Long userId, boolean isEnabled);

    void setTermsOfUseAcceptedFlag(Long userId, boolean termsOfUseAcceptedFlag);

    void setTermsOfUseAcceptedFlagByUsername(String username, boolean termsOfUseAcceptedFlag);

    void setPrivacyPolicyAcceptedFlag(Long userId, boolean privacyPolicyAcceptedFlag);

    void setPrivacyPolicyAcceptedFlagByUsername(String username, boolean privacyPolicyAcceptedFlag);

    void setUserLanguage(Long userId, final String defaultLanguage);

    void setUserTheme(Long userId, final String defaultTheme);

    List<UserView> findAllUsersWithAdminRole();

    List<UserView> findUsersWithRoleSystemAdminAndOperator();

    boolean isUserAdminInAnyDomainById(List<Long> domainIds, String username);

    boolean isUserAdminInAnyDomain(List<Domain> domain, String username);

    boolean isAdmin(String username);

    Page<UserListEntry> findAllListEntry(Pageable pageable, String searchValue);

    Page<UserListEntry> findAllInDomainListEntry(Long domainId, Pageable pageable, String searchValue);

    Optional<Role> getUserRoleInDomain(Long userId, Long domainId);

}