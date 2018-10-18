package net.geant.nmaas.nmservice.deployment.entities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DockerHostTest {

    @Test
    public void shouldBeEqual() {
        final String commonName = "name";
        assertEquals(new DockerHost(commonName), new DockerHost(commonName));
    }

}
