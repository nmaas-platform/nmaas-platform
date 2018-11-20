package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
	
	UserRepository userRepo;
	
	UserRoleRepository userRoleRepo;

	@Autowired
	public UserServiceImpl(UserRepository userRepo, UserRoleRepository userRoleRepo){
		this.userRepo = userRepo;
		this.userRoleRepo = userRoleRepo;
	}
	
	@Override
	public boolean hasPrivilege(User user, Domain domain, Role role) {
		if(user == null || domain == null || role == null)
			return false;
		
		UserRole userRole = userRoleRepo.findByDomainAndUserAndRole(domain, user, role);		
		
		return (userRole != null);
	}

	@Override
	public List<User> findAll() {		
		return userRepo.findAll();
	}

	@Override
	public Page<User> findAll(Pageable pageable) {		
		return userRepo.findAll(pageable);
	}



	@Override
	public Optional<User> findByUsername(String username) {
		return (username != null ? userRepo.findByUsername(username) : Optional.empty());
	}
	
	@Override
	public Optional<User> findById(Long id) {
		return (id != null ? userRepo.findById(id) : Optional.empty());
	}

	@Override
	public Optional<User> findBySamlToken(String token) {
		return (token != null ? userRepo.findBySamlToken(token) : Optional.empty());
	}

	@Override
	public User findByEmail(String email){
		return userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User with mail "+email+ " not found"));
	}

	@Override
	public boolean existsByUsername(String username) {
		checkParam(username);
		return userRepo.existsByUsername(username);
	}

	@Override
	public boolean existsById(Long id) {
		checkParam(id);
		return userRepo.existsById(id);
	}

	@Override
	public User register(String username, Domain domain) {
		checkParam(username);
		return register(username, false, null, domain);
	}

	@Override
	public User register(String username, boolean enabled, String password, Domain domain) {

		checkParam(username);

		// check if user already exists
		Optional<User> user = userRepo.findByUsername(username);
		if(user.isPresent())
			throw new ObjectAlreadyExistsException("User already exists.");

		User newUser = new User(username, enabled, password, domain, Role.ROLE_GUEST);
		
		return userRepo.save(newUser);				
	}	
	
	@Override
	public void update(User user) {
		checkParam(user);
		checkParam(user.getId());
				
		if(!userRepo.existsById(user.getId()))
			throw new ProcessingException("User (id=" + user.getId() + " does not exists.");
		
		userRepo.saveAndFlush(user);
	}

	@Override
	public void delete(User user) {
		checkParam(user);
		checkParam(user.getId());
		
		userRepo.delete(user);
	}


	@Override
	@Transactional
	public void setEnabledFlag(Long userId, boolean isEnabled) {
		userRepo.setEnabledFlag(userId, isEnabled);
	}

	@Override
	@Transactional
	public void setTermsOfUseAcceptedFlag(Long userId, boolean termsOfUseAcceptedFlag){ userRepo.setTermsOfUseAcceptedFlag(userId, termsOfUseAcceptedFlag);}

	@Override
	@Transactional
	public void setTermsOfUseAcceptedFlagByUsername(String username, boolean termsOfUseAcceptedFlag) {
		User user = userRepo.findByUsername(username).orElseThrow(()
				-> new UsernameNotFoundException("User " + username + " not found."));
		userRepo.setTermsOfUseAcceptedFlag(user.getId(), termsOfUseAcceptedFlag);
	}

	@Override
	@Transactional
	public void setPrivacyPolicyAcceptedFlag(Long userId, boolean privacyPolicyAcceptedFlag){ userRepo.setPrivacyPolicyAcceptedFlag(userId, privacyPolicyAcceptedFlag);}

	@Override
	@Transactional
	public void setPrivacyPolicyAcceptedFlagByUsername(String username, boolean privacyPolicyAcceptedFlag) {
		User user = userRepo.findByUsername(username).orElseThrow(()
				-> new UsernameNotFoundException("User " + username + " not found."));
		userRepo.setPrivacyPolicyAcceptedFlag(user.getId(), privacyPolicyAcceptedFlag);
	}

	private void checkParam(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}
	
	private void checkParam(String username) {
		if(username == null)
			throw new IllegalArgumentException("username is null");
	}
	
	private void checkParam(User user) {
		if(user == null)
			throw new IllegalArgumentException("user is null");
	}

	@Override
	public String findAllUsersEmailWithAdminRole(){
        String emails = "";
        for(User user: findAll()){
            for(UserRole userRole: user.getRoles()){
                if(userRole.getRole().name().equalsIgnoreCase(Role.ROLE_SYSTEM_ADMIN.name())){
                    emails = emails + userRole.getUser().getEmail() + ",";
                }
            }
        }
        return emails;
	}

	@Override
	public List<User> findUsersWithRoleSystemAdminAndOperator(){
		List<User> users = new ArrayList<>();
		for(User user : findAll()) {
			for (UserRole userRole : user.getRoles()) {
				if (userRole.getRole().name().equalsIgnoreCase(Role.ROLE_SYSTEM_ADMIN.name()) ||
						userRole.getRole().name().equalsIgnoreCase(Role.ROLE_OPERATOR.name())) {
					users.add(user);
				}
			}
		}
		return users;
	}
}