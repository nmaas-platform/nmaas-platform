package net.geant.nmaas.portal.persistent.repositories;

import java.util.List;
import java.util.Optional;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationBaseRepository extends JpaRepository<ApplicationBase, Long> {

    boolean existsByName(String name);

    Optional<ApplicationBase> findByName(String name);

    @Query("select distinct ab.name FROM ApplicationBase ab")
    List<String> findAllNames();

}
