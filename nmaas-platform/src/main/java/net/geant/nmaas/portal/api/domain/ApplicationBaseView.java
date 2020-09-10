package net.geant.nmaas.portal.api.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

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
    protected List<AppDescriptionView> descriptions;
    protected AppRateView rate;
    @Builder.Default
    protected Set<ApplicationVersionView> appVersions = new HashSet<>();
    @Builder.Default
    protected Set<String> tags = new HashSet<>();

}
