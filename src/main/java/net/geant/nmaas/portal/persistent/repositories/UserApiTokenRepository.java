package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.UserApiToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserApiTokenRepository extends JpaRepository<UserApiToken, Long> {
    List<UserApiToken> findAllByUserId(Long userId);
}
