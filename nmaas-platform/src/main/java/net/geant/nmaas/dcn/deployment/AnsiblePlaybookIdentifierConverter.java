package net.geant.nmaas.dcn.deployment;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

public class AnsiblePlaybookIdentifierConverter {

    private static final String CLIENT_SIDE_ROUTER_PREFIX = "CLIENT-ROUTER";

    private static final String CLOUD_SIDE_ROUTER_PREFIX = "CLOUD-ROUTER";

    public static String encodeForClientSideRouter(String plainClientId) {
        return encode(CLIENT_SIDE_ROUTER_PREFIX + plainClientId);
    }

    public static String encodeForCloudSideRouter(String plainClientId) {
        return encode(CLOUD_SIDE_ROUTER_PREFIX + plainClientId);
    }

    private static String encode(String plainString) {
        return DatatypeConverter.printBase64Binary(plainString.getBytes(Charset.forName("UTF-8")));
    }

    public static String decode(String encodedStringWithPrefix) {
        String decodedStringWithPrefix = decodeString(encodedStringWithPrefix);
        if (decodedStringWithPrefix.startsWith(CLIENT_SIDE_ROUTER_PREFIX))
            return decodedStringWithPrefix.replace(CLIENT_SIDE_ROUTER_PREFIX, "");
        else if (decodedStringWithPrefix.startsWith(CLOUD_SIDE_ROUTER_PREFIX))
            return decodedStringWithPrefix.replace(CLOUD_SIDE_ROUTER_PREFIX, "");
        else
            throw new AnsiblePlaybookIdentifierConverterException("Unrecognized playbook identifier format after decoding: " + decodedStringWithPrefix);
    }

    public static boolean wasEncodedForClientSideRouter(String encodedStringWithPrefix) {
        return decodeString(encodedStringWithPrefix).startsWith(CLIENT_SIDE_ROUTER_PREFIX);
    }

    public static boolean wasEncodedForCloudSideRouter(String encodedStringWithPrefix) {
        return decodeString(encodedStringWithPrefix).startsWith(CLOUD_SIDE_ROUTER_PREFIX);
    }

    private static String decodeString(String encodedString) {
        return new String(DatatypeConverter.parseBase64Binary(encodedString));
    }

    public static class AnsiblePlaybookIdentifierConverterException extends RuntimeException {
        public AnsiblePlaybookIdentifierConverterException(String message) {
            super(message);
        }
    }
}
