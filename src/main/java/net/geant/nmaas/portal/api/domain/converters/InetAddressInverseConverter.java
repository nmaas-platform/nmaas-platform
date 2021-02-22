package net.geant.nmaas.portal.api.domain.converters;

import org.modelmapper.AbstractConverter;

import java.net.InetAddress;

public class InetAddressInverseConverter extends AbstractConverter<InetAddress, String>{

    @Override
    protected String convert(InetAddress source) {
        return source.getHostAddress();
    }

}

