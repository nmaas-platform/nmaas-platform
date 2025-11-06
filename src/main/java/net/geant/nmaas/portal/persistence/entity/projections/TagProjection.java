package net.geant.nmaas.portal.persistence.entity.projections;

import org.springframework.beans.factory.annotation.Value;

public interface TagProjection {
	@Value("#{target.id}")
	Long getId();
	String getName();
}
