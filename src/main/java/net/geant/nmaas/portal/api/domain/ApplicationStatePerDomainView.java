package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatePerDomainView {
    Long applicationBaseId;
    String applicationBaseName;
    boolean enabled;
    long pvStorageSizeLimit;
}
