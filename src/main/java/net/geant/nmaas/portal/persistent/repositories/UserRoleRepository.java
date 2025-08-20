package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.Id> {
	
	@Query("SELECT ur FROM UserRole ur WHERE ur.id.domain = ?1 AND ur.id.user = ?2 AND ur.id.role = ?3")
	UserRole findByDomainAndUserAndRole(Domain domain, User user, Role role);

	@Query("SELECT ur.id.role FROM UserRole ur WHERE ur.id.domain.id = ?1 AND ur.id.user.id = ?2")
	Set<Role> findRolesByDomainAndUser(Long domainId, Long userId);

	@Query("SELECT ur.id.role FROM UserRole ur WHERE ur.id.domain.id = ?1 AND ur.id.user.username = ?2")
	Set<Role> findRolesByDomainAndUser(Long domainId, String username);
	
	@Query("SELECT DISTINCT id.user FROM UserRole ur WHERE ur.id.domain.name = ?1")
	List<User> findDomainMembers(String domainName);
	
	@Query("SELECT DISTINCT id.user FROM UserRole ur WHERE ur.id.domain.id = ?1")
	List<User> findDomainMembers(Long id);

	@Query("SELECT DISTINCT id.user FROM UserRole ur WHERE ur.id.domain.id = ?1 AND ur.id.user.id = ?2")
	Optional<User> findDomainMember(Long domainId, Long userId);
	
	@Modifying
	@Query("DELETE FROM UserRole ur WHERE ur.id.user.id = ?1 AND ur.id.domain.id = ?2")
	void deleteBy(Long userId, Long domainId);
	
	@Modifying
	@Query("DELETE FROM UserRole ur WHERE ur.id.user.id = ?1 AND ur.id.domain.id = ?2 AND ur.id.role = ?3")
	void deleteBy(Long userId, Long domainId, Role role);

}
