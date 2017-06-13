package net.geant.nmaas.portal.persistent.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import net.geant.nmaas.portal.persistent.entity.AppRate;
import net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId;
import net.geant.nmaas.portal.persistent.entity.Application;

public interface RatingRepository extends JpaRepository<AppRate, AppRateId> {

	@Query("select avg(ar.rate) from AppRate ar where ar.appRateId.applicationId = ?1")
	//@Query("select sum(ar.rate) / count(ar.rate) from AppRating ar group by ar.appRatingId.applicationId = ?1")
	Double getApplicationRating(Long appId);
	
}
