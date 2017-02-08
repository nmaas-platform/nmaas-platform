package net.geant.nmaas.dcndeployment;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

public class DcnIdentifierConverter {

    public static String encode(String plainDcnIdentifier) {
        return DatatypeConverter.printBase64Binary(plainDcnIdentifier.getBytes(Charset.forName("UTF-8")));
    }

    public static String decode(String encodedDcnIdentifier) {
        return new String(DatatypeConverter.parseBase64Binary(encodedDcnIdentifier));
    }

}
