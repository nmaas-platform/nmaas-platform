package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    protected Set<ApplicationVersionView> appVersions = new HashSet<>();
    protected Set<String> tags = new HashSet<>();

}
