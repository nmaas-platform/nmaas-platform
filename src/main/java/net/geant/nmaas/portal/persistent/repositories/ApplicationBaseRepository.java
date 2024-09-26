package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.api.domain.ApplicationBaseS;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationBaseRepository extends JpaRepository<ApplicationBase, Long> {

    boolean existsByName(String name);

    Optional<ApplicationBase> findByName(String name);

    @Query("SELECT DISTINCT ab.name FROM ApplicationBase ab")
    List<String> findAllNames();

    @Query("SELECT COUNT(DISTINCT ab.name) FROM ApplicationBase ab JOIN Application a on a.name = ab.name WHERE a.state = 'ACTIVE'")
    long countAllActive();

    @Query(value = "SELECT DISTINCT(ab.*) FROM application_base ab JOIN application_base_versions abv on abv.application_base_id = ab.id JOIN application_version av ON av.id = abv.versions_id WHERE av.state = 'ACTIVE'", nativeQuery = true)
    List<ApplicationBaseS> findAllSmall();

    @Cacheable("applicationBaseS")
    @Query("SELECT ab FROM ApplicationBase ab JOIN Application a on a.name = ab.name WHERE a.state = 'ACTIVE'")
    List<ApplicationBaseS> findAllSmall2();

    @Query("SELECT ab.tags FROM ApplicationBase ab WHERE ab.id =?1")
    List<Tag> findAllBaseTag(Long baseId);

    @Query("SELECT ab.descriptions FROM ApplicationBase ab WHERE ab.id =?1")
    List<AppDescription> findAllBaseDescription(Long baseId);

}