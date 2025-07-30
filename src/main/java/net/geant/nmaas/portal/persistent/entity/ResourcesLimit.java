package net.geant.nmaas.portal.persistent.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.api.domain.DomainView;

@Entity
@Table(name = "resources_limit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourcesLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer memory;

    private Integer cpu;

    private Integer instancesNo;

    private Integer containersNo;

    private boolean isGlobal = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_group_id")
    private DomainGroup domainGroup;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id")
    private Domain domain;

    public ResourcesLimit (Long id, Integer memory, Integer cpu, Integer instancesNo, Integer containersNo, Domain domain){
        this.id = id;
        this.memory= memory;
        this.cpu = cpu;
        this.instancesNo = instancesNo;
        this.containersNo = containersNo;
        this.domain = domain;
    }


}
