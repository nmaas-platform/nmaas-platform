package net.geant.nmaas.portal.api.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ApplicationBriefView {
	Long id;
	
	String name;
	List<ApplicationVersionView> appVersions = new ArrayList<>();
	
	String license;
	String licenseUrl;

	String wwwUrl;
	String sourceUrl;
	String issuesUrl;

	String owner;
	
	List<AppDescriptionView> descriptions;

	ApplicationState state;

	Set<String> tags = new HashSet<>();

}
