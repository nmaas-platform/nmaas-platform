package net.geant.nmaas.portal.persistent.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import net.geant.nmaas.portal.persistent.entity.AppRate;
import net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId;

public interface RatingRepository extends JpaRepository<AppRate, AppRateId> {

	@Query("select ar.rate from AppRate ar where ar.appRateId.applicationId = ?1")
	Integer[] getApplicationRating(Long appId);
	
}
