package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableSet;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.portal.api.auth.Registration;
import net.geant.nmaas.portal.api.auth.UserSSOLogin;
import net.geant.nmaas.portal.api.exception.SignupException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class UserServiceImpl implements UserService {
	
	UserRepository userRepo;
	
	UserRoleRepository userRoleRepo;

	PasswordEncoder passwordEncoder;

	@Autowired
	public UserServiceImpl(UserRepository userRepo, UserRoleRepository userRoleRepo, PasswordEncoder passwordEncoder){
		this.userRepo = userRepo;
		this.userRoleRepo = userRoleRepo;
		this.passwordEncoder = passwordEncoder;
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
	public boolean existsByEmail(String email) {
		return userRepo.existsByEmail(email);
	}

	@Override
	public boolean existsById(Long id) {
		checkParam(id);
		return userRepo.existsById(id);
	}

	@Override
	public User register(Registration registration, Domain globalDomain, Domain domain){

		if(userRepo.existsByUsername(registration.getUsername()) || userRepo.existsByEmail(registration.getEmail())){
			throw new SignupException("REGISTRATION.USER_ALREADY_EXISTS_MESSAGE");
		}

		User newUser = new User(registration.getUsername(), false, passwordEncoder.encode(registration.getPassword()), globalDomain, Role.ROLE_GUEST);
		newUser.setEmail(registration.getEmail());
		newUser.setFirstname(registration.getFirstname());
		newUser.setLastname(registration.getLastname());
		newUser.setEnabled(false);
		if(domain != null){
			newUser.setNewRoles(ImmutableSet.of(new UserRole(newUser, domain, Role.ROLE_GUEST)));
		}
		newUser.setTermsOfUseAccepted(registration.getTermsOfUseAccepted());
		newUser.setPrivacyPolicyAccepted(registration.getPrivacyPolicyAccepted());
		userRepo.save(newUser);

		return newUser;
	}

	@Override
	public User register(UserSSOLogin userSSO, Domain globalDomain){
		byte[] array = new byte[16]; // random password
		new SecureRandom().nextBytes(array);
		String generatedString = new String(array, Charset.forName("UTF-8"));
		User newUser = new User("thirdparty-"+System.currentTimeMillis(), true, generatedString, globalDomain, Role.ROLE_INCOMPLETE);
		newUser.setSamlToken(userSSO.getUsername()); //Check user ID TODO: check if it's truly unique!
		userRepo.save(newUser);

		return newUser;
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