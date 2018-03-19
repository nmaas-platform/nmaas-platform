package net.geant.nmaas.portal.persistent.entity.projections;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

public interface ApplicationBriefProjection {
	
	@Value("#{target.id}")
	Long getId();
	String getName();
	String getVersion();
	String getLicense();
	String getWwwUrl();
	String getSourceUrl();
	String getIssuesUrl();
	String getBriefDescription();
	
	Set<TagProjection> getTags();	
	
}
