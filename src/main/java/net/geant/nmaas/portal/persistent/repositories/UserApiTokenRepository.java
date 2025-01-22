package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.UserApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserApiTokenRepository extends JpaRepository<UserApiToken, Long> {
    List<UserApiToken> findAllByUserId(Long userId);

    List<UserApiToken> findAllByUserIdAndName(Long userId, String name);
}
