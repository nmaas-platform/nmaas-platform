package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * medium ApplicationBase & Application DTO
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ApplicationBriefView extends ApplicationBaseView {

	// application base properties
	
	String license;
	String licenseUrl;

	String wwwUrl;
	String sourceUrl;
	String issuesUrl;
	String nmaasDocumentationUrl;

	// application properties

	String owner;

	ApplicationState state;

}
