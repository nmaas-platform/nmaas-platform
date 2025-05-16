package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class DomainBase  implements Serializable {

    Long id;
    String name;
    String codename;
    boolean active;
    boolean deleted;

}