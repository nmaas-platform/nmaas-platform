package net.geant.nmaas.portal.persistent.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.entity.projections.ApplicationBriefProjection;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
	
	
	Optional<ApplicationBriefProjection> findApplicationBriefById(@Param("id") Long id);
	
	@Query("select app from Application app")
	List<ApplicationBriefProjection> findApplicationBriefAll();
	
	@Query("select app from Application app")
	Page<ApplicationBriefProjection> findApplicationBriefAll(Pageable pageable);
	
	List<ApplicationBriefProjection> findApplicationBriefAllByIdIn(List<Long> ids);
	
	
	
	List<Application> findByName(String name);
	
	List<Application> findByTags(Tag tag);
}
