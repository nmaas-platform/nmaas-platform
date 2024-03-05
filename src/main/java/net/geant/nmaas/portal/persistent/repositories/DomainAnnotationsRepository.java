package net.geant.nmaas.portal.persistent.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.api.domain.KeyValueView;
import net.geant.nmaas.portal.persistent.entity.DomainAnnotation;

import java.util.Optional;



@Repository
public interface DomainAnnotationsRepository extends JpaRepository<DomainAnnotation, Long> {

    // @Query("SELECT COUNT(kv) > 0 FROM domain_annotations kv WHERE kv.key_string = :key")
    boolean existsByKey(@Param("key") String key);

    // @Query("SELECT da from KeyValue da WHERE da.keyString = :key")
    Optional<DomainAnnotation> findByKey(@Param("key") String key);
} 
