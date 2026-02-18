package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.ResourcesLimitDto;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimit;
import net.geant.nmaas.portal.persistence.entity.ResourcesLimitType;
import org.modelmapper.AbstractConverter;

public class ResourceLimitInverseConverter extends AbstractConverter<ResourcesLimitDto, ResourcesLimit> {

    @Override
    protected ResourcesLimit convert(ResourcesLimitDto source) {
        ResourcesLimit resourcesLimit = new ResourcesLimit();
        resourcesLimit.setId(source.id());
        resourcesLimit.setLimitType(ResourcesLimitType.from(source.limitType()));
        resourcesLimit.setMemory(source.memory());
        resourcesLimit.setCpu(source.cpu());
        resourcesLimit.setInstancesNo(source.instancesNo());
        resourcesLimit.setContainersNo(source.containersNo());
        switch (source.limitType()) {
            case GLOBAL -> {
                resourcesLimit.setDomain(null);
                resourcesLimit.setDomainGroup(null);
            }
            case DOMAIN -> resourcesLimit.setDomainGroup(null);
            case DOMAIN_GROUP -> resourcesLimit.setDomain(null);
        }
        return resourcesLimit;
    }

}

