package net.geant.nmaas.portal.api.shell;

import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

public class SshConnectionShellSessionObservableTest {

    public final String PUB_KEY = SshConnectionShellSessionObservable.SSH_PUB_KEY_X509;
    public final String PRIV_KEY = SshConnectionShellSessionObservable.SSH_PRIV_KEY;

    @Test
    public void testPublicKeyConversion() throws InvalidKeySpecException, NoSuchAlgorithmException {
        PublicKey publicKey = SshConnectionShellSessionObservable.getPublicKey(PUB_KEY);
        System.out.println(publicKey);
    }

    @Test
    public void testPrivateKeyConversion() throws InvalidKeySpecException, NoSuchAlgorithmException {
        PrivateKey privateKey = SshConnectionShellSessionObservable.getPrivateKey(PRIV_KEY);
        System.out.println(privateKey);
    }
}
