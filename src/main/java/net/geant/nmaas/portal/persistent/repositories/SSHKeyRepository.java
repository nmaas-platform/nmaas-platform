package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SSHKeyRepository extends JpaRepository<SSHKeyEntity, Long> {

    List<SSHKeyEntity> findAllByOwner(User owner);
    boolean existsByOwnerAndName(User owner, String name);
}
