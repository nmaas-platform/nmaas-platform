package net.geant.nmaas.portal.persistent.entity;

import lombok.*;
import net.geant.nmaas.portal.api.domain.ApplicationStatePerDomainView;

import javax.persistence.CascadeType;
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

    @ManyToOne(fetch = FetchType.LAZY)
//    @NotNull
    @Setter(AccessLevel.PROTECTED)
    private ApplicationBase applicationBase;


    /*
    in future this can be replaced with custom state object
    it should simplify managing domain related application state issues
     */
    @EqualsAndHashCode.Exclude
    private boolean enabled;

    private long pvStorageSizeLimit;


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

    public ApplicationStatePerDomain(ApplicationBase applicationBase, boolean enabled, long pvStorageSizeLimit){
        super();
        this.applicationBase = applicationBase;
        this.enabled = enabled;
        this.pvStorageSizeLimit = pvStorageSizeLimit;
    }

    public void applyChangedState(ApplicationStatePerDomainView appStateView){
        this.enabled = appStateView.isEnabled();
        this.pvStorageSizeLimit = appStateView.getPvStorageSizeLimit();
    }
}
