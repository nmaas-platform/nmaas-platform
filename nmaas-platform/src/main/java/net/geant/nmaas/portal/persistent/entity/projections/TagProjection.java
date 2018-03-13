package net.geant.nmaas.portal.persistent.entity.projections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.web.ProjectedPayload;

import net.geant.nmaas.portal.persistent.entity.Tag;

public interface TagProjection {
	@Value("#{target.id}")
	Long getId();
	String getName();
}
