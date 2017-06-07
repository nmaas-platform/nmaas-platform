package net.geant.nmaas.dcn.deployment;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsiblePlaybookIdentifierConverter {

    private static final String CLIENT_SIDE_ROUTER_PREFIX = "CLIENT-ROUTER";

    private static final String CLOUD_SIDE_ROUTER_PREFIX = "CLOUD-ROUTER";

    public static String encodeForClientSideRouter(String plainDeploymentId) {
        return encode(CLIENT_SIDE_ROUTER_PREFIX + plainDeploymentId);
    }

    public static String encodeForCloudSideRouter(String plainDeploymentId) {
        return encode(CLOUD_SIDE_ROUTER_PREFIX + plainDeploymentId);
    }

    private static String encode(String plainString) {
        return DatatypeConverter.printBase64Binary(plainString.getBytes(Charset.forName("UTF-8")));
    }

    public static String decode(String encodedStringWithPrefix) throws AnsiblePlaybookIdentifierConverterException {
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

    public static class AnsiblePlaybookIdentifierConverterException extends Exception {
        public AnsiblePlaybookIdentifierConverterException(String message) {
            super(message);
        }
    }
}
