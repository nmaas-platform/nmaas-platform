package net.geant.nmaas.portal.api.domain.converters;

import org.modelmapper.AbstractConverter;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressConverter extends AbstractConverter<String, InetAddress>{

    @Override
    protected InetAddress convert(String source) {
        try {
            return InetAddress.getByName(source);
        } catch (UnknownHostException e) {
            return null;
        }
    }

}

