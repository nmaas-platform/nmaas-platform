package net.geant.nmaas.portal.api.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationBriefView {
	Long id;
	
	String name;
	String version;
	
	String license;
	String licenseUrl;

	String wwwUrl;
	String sourceUrl;
	String issuesUrl;
	
	List<AppDescriptionView> descriptions;

	Set<String> tags = new HashSet<>();

}
