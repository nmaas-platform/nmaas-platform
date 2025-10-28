package net.geant.nmaas.portal.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ApplicationStatePerDomainView  implements Serializable {
    Long applicationBaseId;
    String applicationBaseName;
    boolean enabled;
    long pvStorageSizeLimit;
}
