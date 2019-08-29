package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ApplicationVersionView {
    private String version;
    private ApplicationState state;
    private Long appVersionId;
}
