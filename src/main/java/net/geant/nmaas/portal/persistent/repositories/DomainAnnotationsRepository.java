package net.geant.nmaas.portal.persistent.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.DomainAnnotation;

import java.util.Optional;



@Repository
public interface DomainAnnotationsRepository extends JpaRepository<DomainAnnotation, Long> {

    boolean existsByKey(@Param("key") String key);

    Optional<DomainAnnotation> findByKey(@Param("key") String key);
} 
