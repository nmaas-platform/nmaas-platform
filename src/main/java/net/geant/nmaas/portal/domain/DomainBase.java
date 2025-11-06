package net.geant.nmaas.portal.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.Domain;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DomainBase implements Serializable {

    Long id;
    String name;
    String codename;
    boolean active;
    boolean deleted;

    public static DomainBase fromEntity(Domain domain) {
        return new DomainBase(domain.getId(), domain.getName(), domain.getCodename(), domain.isActive(), domain.isDeleted());
    }

    public static DomainBase fromView(DomainView domain) {
        return new DomainBase(domain.getId(), domain.getName(), domain.getCodename(), domain.isActive(), domain.isDeleted());
    }

}