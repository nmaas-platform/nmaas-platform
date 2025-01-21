package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.UserApiTokens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserApiTokensRepository extends JpaRepository<UserApiTokens, Long> {
    List<UserApiTokens> findAllByUserId(Long userId);
}
