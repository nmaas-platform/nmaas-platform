package net.geant.nmaas.portal.persistent.entity;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class ApplicationStatePerDomain {

    /*
    in future this can be replaced with custom state object
    it should simplify managing domain related application state issues
     */
    @EqualsAndHashCode.Exclude
    private boolean enabled;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private ApplicationBase applicationBase;

    public ApplicationStatePerDomain(ApplicationBase applicationBase){
        super();
        this.applicationBase = applicationBase;
        this.enabled = true;
    }

    public ApplicationStatePerDomain(ApplicationBase applicationBase, boolean enabled){
        super();
        this.applicationBase = applicationBase;
        this.enabled = enabled;
    }

}
