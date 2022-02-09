package net.geant.nmaas.utils.ssh;

import net.schmizz.sshj.transport.verification.HostKeyVerifier;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;

public class DummyHostKeyVerifier implements HostKeyVerifier {

    @Override
    public boolean verify(String hostname, int port, PublicKey key) {
        return true;
    }

    @Override
    public List<String> findExistingAlgorithms(String hostname, int port) {
        return Collections.emptyList();
    }

}
