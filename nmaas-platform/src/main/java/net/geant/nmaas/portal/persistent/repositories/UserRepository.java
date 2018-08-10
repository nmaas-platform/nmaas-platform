package net.geant.nmaas.portal.persistent.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	boolean existsByUsername(String username);
	Optional<User> findByUsername(String username);
	Optional<User> findBySamlToken(String token);

    @Modifying
    @Query("update User u set u.enabled = ?2 where u.id = ?1")
    void setEnabledFlag(Long userId,  boolean isEnabledFlag);
}
