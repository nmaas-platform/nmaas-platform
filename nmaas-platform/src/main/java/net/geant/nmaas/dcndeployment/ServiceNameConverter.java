package net.geant.nmaas.dcndeployment;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

public class ServiceNameConverter {

    public static String encode(String serviceName) {
        return DatatypeConverter.printBase64Binary(serviceName.getBytes(Charset.forName("UTF-8")));
    }

    public static String decode(String serviceId) {
        return new String(DatatypeConverter.parseBase64Binary(serviceId));
    }

}
