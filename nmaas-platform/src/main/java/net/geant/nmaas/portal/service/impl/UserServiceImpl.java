package net.geant.nmaas.portal.service.impl;

import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.exceptions.ObjectAlreadyExistsException;
import net.geant.nmaas.portal.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements net.geant.nmaas.portal.service.UserService {

	@Autowired
	DomainService domains;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	UserRoleRepository userRoleRepo;
	
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
	public User register(String username) throws ObjectAlreadyExistsException, MissingElementException {
		checkParam(username);
		return register(username, false, null, null);				
	}

	@Override
	public User register(String username, boolean enabled, String password, Long domainId) throws ObjectAlreadyExistsException, MissingElementException {

		checkParam(username);
		
		Domain domain = (domainId != null ? domains.findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found")) 
											: domains.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found")));

		// check if user already exists
		Optional<User> user = userRepo.findByUsername(username);
		if(user.isPresent())
			throw new ObjectAlreadyExistsException("User already exists.");

		User newUser = new User(username, enabled, password, domain, Role.ROLE_GUEST);
		
		return userRepo.save(newUser);				
	}	
	
	@Override
	public void update(User user) throws ProcessingException {
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
	public void setTermsOfUseAcceptedFlag(Long userId, boolean touAccept){ userRepo.setTermsOfUseAcceptedFlag(userId, touAccept);}

	@Override
	@Transactional
	public void setTermsOfUseAcceptedFlagByUsername(String username, boolean touAccept) throws UsernameNotFoundException{
		User user = userRepo.findByUsername(username).orElseThrow(()
				-> new UsernameNotFoundException("User " + username + " not found."));
		userRepo.setTermsOfUseAcceptedFlag(user.getId(), touAccept);
	}

	@Override
	@Transactional
	public void setPrivacyPolicyAcceptedFlag(Long userId, boolean privacyPolicyAcceptedFlag){ userRepo.setPrivacyPolicyAcceptedFlag(userId, privacyPolicyAcceptedFlag);}

	@Override
	@Transactional
	public void setPrivacyPolicyAcceptedFlagByUsername(String username, boolean privacyPolicyAcceptedFlag) throws UsernameNotFoundException{
		User user = userRepo.findByUsername(username).orElseThrow(()
				-> new UsernameNotFoundException("User " + username + " not found."));
		userRepo.setPrivacyPolicyAcceptedFlag(user.getId(), privacyPolicyAcceptedFlag);
	}

	protected void checkParam(Long id) {
		if(id == null)
			throw new IllegalArgumentException("id is null");
	}
	
	protected void checkParam(String username) {
		if(username == null)
			throw new IllegalArgumentException("username is null");
	}
	
	protected void checkParam(User user) {
		if(user == null)
			throw new IllegalArgumentException("user is null");
	}

}
