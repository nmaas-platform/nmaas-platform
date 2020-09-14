package net.geant.nmaas.portal.api.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * minimal ApplicationBase DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApplicationBaseView {

    protected Long id;
    protected String name;

    protected String license;
    protected String licenseUrl;

    protected String wwwUrl;
    protected String sourceUrl;
    protected String issuesUrl;
    protected String nmaasDocumentationUrl;

    // logo
    // screenshots

    @Builder.Default
    protected List<AppDescriptionView> descriptions = new ArrayList<>();
    @Builder.Default
    protected Set<String> tags = new HashSet<>();

    //comments

    @Builder.Default
    protected Set<ApplicationVersionView> versions = new HashSet<>();


    // rating info
    protected AppRateView rate;

}
