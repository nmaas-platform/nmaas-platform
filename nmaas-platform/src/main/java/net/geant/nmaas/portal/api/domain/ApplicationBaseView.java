package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class ApplicationBaseView {

    private Long id;
    private String name;
    private List<AppDescriptionView> descriptions;
    private AppRateView rate;
    private Set<ApplicationVersionView> appVersions = new HashSet<>();
    private Set<String> tags = new HashSet<>();

}
