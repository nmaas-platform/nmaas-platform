package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.domains.CustomerNetworkDto;
import net.geant.nmaas.dcn.deployment.entities.CustomerNetwork;
import org.modelmapper.AbstractConverter;

public class CustomerNetworkConverter extends AbstractConverter<CustomerNetwork, CustomerNetworkDto> {

    @Override
    protected CustomerNetworkDto convert(CustomerNetwork source) {
        return new CustomerNetworkDto(source.getId(), source.getCustomerIp(), source.getMaskLength());
    }

}