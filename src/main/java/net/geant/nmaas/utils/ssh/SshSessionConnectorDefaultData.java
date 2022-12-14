package net.geant.nmaas.utils.ssh;

import net.schmizz.sshj.userauth.keyprovider.KeyPairWrapper;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * hardcoded ssh keys
 */
public class SshSessionConnectorDefaultData {

    public static final String SSH_PUB_KEY_X509 =
            "-----BEGIN PUBLIC KEY-----\n" +
                    "MIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEAo1lfdfK74mV5Xqr7sLYQ\n" +
                    "bp0kF3PZHXzQt6p+J3QuiOVMbe1XEVPZP7QiJqikrGEghIPklYvOSLQE9wcr2mA+\n" +
                    "NyIXWq3QI0zaGyW/C45dqr8o8bOjTos0i0CemYXQh58xKUl77bqytQvknv5oxOAV\n" +
                    "uJrdRNqt3UmSK2VW3cTAbUxXrsC/lYgGYz04VAHylV2VW5tqeWh83BXKIjp3ANkD\n" +
                    "OrfcO1WIUw9c/5yafBbBwkqlIlWO617exR6U/Ad178fTvJ4Shws7NvCpnunaow5Q\n" +
                    "pbk87LDb/R/Hzbo/I+98D94qMx+x9IVA7yvyegw3z6FHKx4RkvUgfk7cz7RuDh+m\n" +
                    "XwIBJQ==\n" +
                    "-----END PUBLIC KEY-----";
    public static final String SSH_PRIV_KEY =
            "-----BEGIN PRIVATE KEY-----\n" +
                    "MIIEugIBADANBgkqhkiG9w0BAQEFAASCBKQwggSgAgEAAoIBAQCjWV918rviZXle\n" +
                    "qvuwthBunSQXc9kdfNC3qn4ndC6I5Uxt7VcRU9k/tCImqKSsYSCEg+SVi85ItAT3\n" +
                    "ByvaYD43IhdardAjTNobJb8Ljl2qvyjxs6NOizSLQJ6ZhdCHnzEpSXvturK1C+Se\n" +
                    "/mjE4BW4mt1E2q3dSZIrZVbdxMBtTFeuwL+ViAZjPThUAfKVXZVbm2p5aHzcFcoi\n" +
                    "OncA2QM6t9w7VYhTD1z/nJp8FsHCSqUiVY7rXt7FHpT8B3Xvx9O8nhKHCzs28Kme\n" +
                    "6dqjDlCluTzssNv9H8fNuj8j73wP3iozH7H0hUDvK/J6DDfPoUcrHhGS9SB+TtzP\n" +
                    "tG4OH6ZfAgElAoIBAAjUZgZgJdTi4/dHgzn8AONicKdSXsNS2tl+1mL/XHYaO3uQ\n" +
                    "SeVCzXkQp+Zp+xA8mflTPMnQNKn75Je7Ms2IqWrDko9HqmLF4kaoGCojXwJPhawz\n" +
                    "OUKEEK2Uyk182tbmAqhJKUse2To3/oUigjQnydKglla/tl79DtHpzVgYeRqqF+WZ\n" +
                    "ImNGQP4cqxWI7OJ7RHgfmh1zOmW4S2pf+a6zD/Z69vnqHKtkQ668jwwb91ySUGbG\n" +
                    "RiPL5h+YMTcebwmb9uYedxOz/t9fL2zARMwMcZrohLmRlCzZJHpI2ygL80zOV5u0\n" +
                    "rDITW51s6tQrPkSXrWeOw0cL+qzQ4BEvC0CaAg0CgYEA7ZE7Uw4BPl6vXstQ+PBG\n" +
                    "KdZeg2cKFfrYGwVdJQHKOluOVVrZe2YQJAsvZzZZe9YEJnJ2z9m1QJr3akqjgAvd\n" +
                    "xL1WiO+dUp9ZTwLJfR11hx6aiqzTJ5DS9VSqFQMPBQlAXce46KP18SMd8olM/iBW\n" +
                    "Wc3HEjqY7Lu7l32V9Q2B+qkCgYEAsAX0cFrzFad7fHpL20cu+Nb0Q/GKrodsO+zt\n" +
                    "xt8eIY9GrdEQ+opAVNPM3OW35buzr0KKrXttfOGnnDWw77Ikt14T7qrr/XM2bUA7\n" +
                    "ZMayk9VRGtbYfy6br6LMU7wDYfm0d6rYV7IpMOuvB5ufObpU5WGF4rX+lkV7FBQ4\n" +
                    "KNV8hccCgYBNDIHxbFNuLIvnZIj2ynee3b3JwI2mQ4RbymNl8r4gw791oOyWuVFX\n" +
                    "zEa5sMMTaAFYk9OBrmRMTe9gvkLda6He8Ux/cE6zAz+PPyyXR9MXENgs+cfxKA0R\n" +
                    "S+QifYhUqQ305tQvxnlHSajZCe91A5GglcQF6X20j+nQRGf8cxVmDQKBgByLWBI4\n" +
                    "QxhErD2y94tXpsCRj2T2vIsBNCVksO/RJ3sQUKaQmvgyGEUbbVRA7WOGONBP+tAG\n" +
                    "LW4ybjUByapUPU4q7nm62ikZmh+N4B4uDx7kUldhHDBMv0zp957gN+Zf2BNn0A44\n" +
                    "kQ7aHGIZPGo55EDfYdG8pdMt3Jt45oMpidCBAoGAY9PPPhnhawR7lU99qkAEp2q/\n" +
                    "hOIQuZVRXOLUS04EE+YaZoGZ7XbMWG48QYk++Kfr8LwbG3wWWHSywnx2kXrEUu7n\n" +
                    "we0cs9qikUog1n5kNUQYzQQwVFMOKG7deP3HQ0Mhwg7VLOyzodryr0sNEq5S+Xqf\n" +
                    "wWiKDSVL4RJDRKFJAdc=\n" +
                    "-----END PRIVATE KEY-----";

    private static final String SSH_USERNAME = "nmaastest";
    private static final String SSH_HOST = "nmaastest-master1.qalab.geant.net";

    private SshSessionConnectorDefaultData(){}

    /**
     * transforms string public key to java format
     * @param pubKey string public key
     * @return Java formatted public key in X509
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PublicKey getPublicKey(String pubKey) throws InvalidKeySpecException, NoSuchAlgorithmException {

        KeyFactory kf = KeyFactory.getInstance("RSA");

        String publicKeyContent = pubKey
                .replaceAll("\\n", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "");

        X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        return kf.generatePublic(keySpecX509);

    }

    /**
     * transforms string private key to java format
     * @param privKey string private key
     * @return Java formatted private key in PKCS8
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey getPrivateKey(String privKey) throws NoSuchAlgorithmException, InvalidKeySpecException {

        KeyFactory kf = KeyFactory.getInstance("RSA");

        String privateKeyContent = privKey
                .replaceAll("\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");

        PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        return kf.generatePrivate(keySpecPKCS8);

    }

    public static SshSessionConnector getDefaultConnector() throws InvalidKeySpecException, NoSuchAlgorithmException {
        PublicKey publicKey = getPublicKey(SshSessionConnectorDefaultData.SSH_PUB_KEY_X509);
        PrivateKey privateKey = getPrivateKey(SshSessionConnectorDefaultData.SSH_PRIV_KEY);
        KeyPair kp = new KeyPair(publicKey, privateKey);

        return new SshSessionConnector(
                SSH_HOST,
                22,
                new BasicCredentials(SSH_USERNAME),
                new KeyPairWrapper(kp)
        );
    }
}
