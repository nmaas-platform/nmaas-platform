package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.SSHKeyEntity;
import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SSHKeyRepository extends JpaRepository<SSHKeyEntity, Long> {

    List<SSHKeyEntity> findAllByOwner(User owner);
    boolean existsByOwnerAndName(User owner, String name);

    boolean existsByKeyValue(@Param("keyValue") String keyValue);
}
