package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.UserApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserApiTokenRepository extends JpaRepository<UserApiToken, Long> {
    List<UserApiToken> findAllByUserId(Long userId);

    List<UserApiToken> findAllByUserIdAndName(Long userId, String name);

    List<UserApiToken> findAllByValid(boolean valid);
}
