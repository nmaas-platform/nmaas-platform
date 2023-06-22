package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DomainBase {
    Long id;

    String name;
    String codename;
    boolean active;
    boolean deleted;
}
