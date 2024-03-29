package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByUsername(String username);
	boolean existsByEmail(String email);
	Optional<User> findByUsername(String username);
	Optional<User> findBySamlToken(String token);
	Optional<User> findByEmail(String email);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.enabled = ?2 where u.id = ?1")
    void setEnabledFlag(Long userId, boolean isEnabledFlag);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.selectedLanguage = ?2 where u.id = ?1")
    void setUserLanguage(Long userId, String userLanguage);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.termsOfUseAccepted = ?2 where u.id = ?1")
    void setTermsOfUseAcceptedFlag(Long userId, boolean termsOfUseAcceptedFlag);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update User u set u.privacyPolicyAccepted = ?2 where u.id = ?1")
    void setPrivacyPolicyAcceptedFlag(Long userId, boolean privacyPolicyAcceptedFlag);

    @Query("SELECT count(distinct u.id) FROM User u JOIN UserRole r ON r.id.user.id = u.id WHERE r.id.domain.id != 1")
    int countWithDomain();

}
