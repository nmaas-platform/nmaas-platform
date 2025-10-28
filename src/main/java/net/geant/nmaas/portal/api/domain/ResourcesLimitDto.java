package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourcesLimitDto extends ResourcesLimitUpdateDto {

    private ResourcesLimitType limitType;
    private DomainGroupViewS domainGroup;
    private DomainBase domain;

    public ResourcesLimitDto (Long id, Integer memory, Integer cpu, Integer instancesNo, Integer containersNo, DomainBase domain){
        this.id = id;
        this.memory= memory;
        this.cpu = cpu;
        this.instancesNo = instancesNo;
        this.containersNo = containersNo;
        this.domain = domain;
        this.limitType= ResourcesLimitType.DOMAIN;
    }
}
