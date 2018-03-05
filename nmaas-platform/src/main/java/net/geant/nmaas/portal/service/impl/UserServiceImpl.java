package net.geant.nmaas.portal.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

@Service
public class UserServiceImpl implements net.geant.nmaas.portal.service.UserService {

	@Autowired
	DomainService domains;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	UserRoleRepository userRoleRepo;
	
	@Override
	public boolean hasPriviledge(User user, Domain domain, Role role) {
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
		return (id != null ? Optional.ofNullable(userRepo.findOne(id)) : Optional.empty());
	}
	
	@Override
	public boolean existsByUsername(String username) {
		checkParam(username);
		return userRepo.existsByUsername(username);
	}

	@Override
	public boolean existsById(Long id) {
		checkParam(id);
		return userRepo.exists(id);
	}

	@Override
	public User register(String username) throws ObjectAlreadyExistsException, MissingElementException {
		checkParam(username);
		return register(username, null, null);				
	}

	@Override
	public User register(String username, String password, Long domainId) throws ObjectAlreadyExistsException, MissingElementException {
		checkParam(username);
		
		Domain domain = (domainId != null ? domains.findDomain(domainId).orElseThrow(() -> new MissingElementException("Domain not found")) 
											: domains.getGlobalDomain().orElseThrow(() -> new MissingElementException("Global domain not found")));

		// check if user already exists
		userRepo.findByUsername(username)
				.ifPresent((user) -> new ObjectAlreadyExistsException("User already exists."));

		User newUser = new User(username, password, domain, Role.ROLE_GUEST);
		
		return userRepo.save(newUser);				
	}	
	
	@Override
	public void update(User user) throws ProcessingException {
		checkParam(user);
		checkParam(user.getId());
				
		if(!userRepo.exists(user.getId()))
			throw new ProcessingException("User (id=" + user.getId() + " does not exists.");
		
		userRepo.saveAndFlush(user);
	}

	@Override
	public void delete(User user) {
		checkParam(user);
		checkParam(user.getId());
		
		userRepo.delete(user);
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
