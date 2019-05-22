package net.geant.nmaas.portal.service;

import java.util.List;
import java.util.Optional;

import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.auth.UserSSOLogin;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;

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

	User register(Registration registration, Domain globalDomain, Domain domain);
	User register(UserSSOLogin userSSO, Domain globalDomain);
	
	List<User> findAll();
	Page<User> findAll(Pageable pageable);

	void delete(User user);	
	void update(User user);
    void setEnabledFlag(Long userId, boolean isEnabled);
    void setTermsOfUseAcceptedFlag(Long userId, boolean termsOfUseAcceptedFlag);
    void setTermsOfUseAcceptedFlagByUsername(String username, boolean termsOfUseAcceptedFlag);
    void setPrivacyPolicyAcceptedFlag(Long userId, boolean privacyPolicyAcceptedFlag);
    void setPrivacyPolicyAcceptedFlagByUsername(String username, boolean privacyPolicyAcceptedFlag);
	void setUserLanguage(Long userId, final String defaultLanguage);
    List<UserView> findAllUsersWithAdminRole();
	List<UserView> findUsersWithRoleSystemAdminAndOperator();
}
