package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.api.domain.ApplicationBaseS;
import net.geant.nmaas.portal.persistent.entity.AppDescription;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationBaseRepository extends JpaRepository<ApplicationBase, Long> {

    boolean existsByName(String name);

    Optional<ApplicationBase> findByName(String name);

    @Query("select distinct ab.name FROM ApplicationBase ab")
    List<String> findAllNames();

    @Query("select count(distinct ab.name) FROM ApplicationBase ab JOIN Application a on a.name = ab.name WHERE a.state = 'ACTIVE'")
    long countAllActive();

//    @Query(value = "select new net.geant.nmaas.portal.api.domain.ApplicationBaseSTest(ab.id, ab.name, ab.tags) from ApplicationBase ab JOIN Application a ON a.name = ab.name " +
//            " WHERE a.state = 'ACTIVE' ", nativeQuery = true)
//    List<ApplicationBaseSTest> findAllSmallQuery();

    @Query(value = "Select ab.* from application_base ab JOIN application_base_versions abv on abv.application_base_id = ab.id JOIN application_version av ON av.id = abv.versions_id WHERE av.state = 'ACTIVE'", nativeQuery = true)
    List<ApplicationBaseS> findAllSmall();

//    @Query(value = "select ad.* FROM app_description ad JOIN application_base_descriptions abd ON ad.id = abd.descriptions_id WHERE abd.application_base_id =:base_id", nativeQuery = true)
//    List<AppDescription> findDescriptionsForBase(Long base_id);
//
//    @Query(value = "select tg.* FROM tag tg JOIN application_base_tag abt ON tg.tag_id = abt.tag_id WHERE abt.application_base_id =?1", nativeQuery = true)
//    List<Object> findTagsForBase(Long base_id);

    @Query("Select ab.tags FROM ApplicationBase ab where ab.id =?1")
    List<Tag> findAllBaseTag(Long base_id);

    @Query("Select ab.descriptions FROM ApplicationBase ab where ab.id =?1")
    List<AppDescription> findAllBaseDescription(Long base_id);
}