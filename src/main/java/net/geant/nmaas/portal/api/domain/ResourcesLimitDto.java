package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourcesLimitDto {

    private Long id;
    private Integer memory;
    private Integer cpu;
    private Integer instancesNo;
    private Integer containersNo;
    private boolean isGlobal = false;
    private DomainGroupViewS domainGroup;
    private DomainBase domain;

    public ResourcesLimitDto (Long id, Integer memory, Integer cpu, Integer instancesNo, Integer containersNo, DomainBase domain){
        this.id = id;
        this.memory= memory;
        this.cpu = cpu;
        this.instancesNo = instancesNo;
        this.containersNo = containersNo;
        this.domain = domain;
    }
}
