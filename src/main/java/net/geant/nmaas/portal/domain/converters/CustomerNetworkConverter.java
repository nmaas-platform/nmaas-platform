package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.domains.CustomerNetworkView;
import net.geant.nmaas.dcn.deployment.entities.CustomerNetwork;
import org.modelmapper.AbstractConverter;

public class CustomerNetworkConverter extends AbstractConverter<CustomerNetwork, CustomerNetworkView> {

    @Override
    protected CustomerNetworkView convert(CustomerNetwork source) {
        return new CustomerNetworkView(source.getId(), source.getCustomerIp(), source.getMaskLength());
    }

}


