package net.geant.nmaas.portal.api.domain;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationBrief {
	Long id;
	
	String name;
	String version;
	
	String license;

	String wwwUrl;
	String sourceUrl;
	String issuesUrl;
	
	String briefDescription;
	
	Set<String> tags = new HashSet<>();

}
