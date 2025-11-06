package net.geant.nmaas.portal.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ApplicationVersionView {
    private String id;
    private String version;
    private ApplicationState state;
    private Long appVersionId;
}
