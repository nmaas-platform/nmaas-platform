package net.geant.nmaas.externalservices.inventory.dockerhosts;

import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
public class DockerHostRepositoryTest {
    private DockerHostRepository dockerHostRepository;
    private final static String DOCKER_HOST_NAME_1 = "TEST-DOCKER-HOST-1";
    private final static String DOCKER_HOST_NAME_2 = "GN4-ANSIBLE-HOST";
    private final static String PREFERRED_DOCKER_HOST_NAME = "GN4-DOCKER-1";
    private final static String VOLUME_PATH = "/new/path";

    @Before
    public void init() {
        dockerHostRepository = new DockerHostRepository();
    }

    @Test
    public void shouldAddDockerHost() throws Exception {
        dockerHostRepository.addDockerHost(initNewDockerHost(DOCKER_HOST_NAME_1));
        assertEquals(initNewDockerHost(DOCKER_HOST_NAME_1), dockerHostRepository.loadByName(DOCKER_HOST_NAME_1));
        dockerHostRepository.removeDockerHost(DOCKER_HOST_NAME_1);
    }

    @Test(expected = DockerHostExistsException.class)
    public void shouldNotAddTheSameDockerHost() throws Exception {
        dockerHostRepository.addDockerHost(initNewDockerHost("GN4-DOCKER-1"));
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotAddDockerHostWithoutName() throws Exception {
        dockerHostRepository.addDockerHost(initNewDockerHost(null));
    }

    @Test(expected = DockerHostNotFoundException.class)
    public void shouldRemoveDockerHost() throws Exception {
        dockerHostRepository.removeDockerHost(DOCKER_HOST_NAME_2);
        dockerHostRepository.loadByName(DOCKER_HOST_NAME_2);
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotRemoveDockerHostWithoutName() throws Exception {
        dockerHostRepository.removeDockerHost(null);
    }

    @Test
    public void shouldUpdateDockerHost() throws Exception {
        DockerHost dockerHost = initNewDockerHost(DOCKER_HOST_NAME_2);
        dockerHost.setVolumesPath(VOLUME_PATH);
        dockerHostRepository.updateDockerHost(DOCKER_HOST_NAME_2, dockerHost);
        assertEquals(VOLUME_PATH, dockerHostRepository.loadByName(DOCKER_HOST_NAME_2).getVolumesPath());
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotUpdateDockerHostWhenNameIsNull() throws Exception {
        dockerHostRepository.updateDockerHost(null, initNewDockerHost(DOCKER_HOST_NAME_2));
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotUpdateDockerHostWithoutInvalidName() throws Exception {
        dockerHostRepository.updateDockerHost(DOCKER_HOST_NAME_1, initNewDockerHost(DOCKER_HOST_NAME_2));
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotUpdateNullDockerHost() throws Exception {
        dockerHostRepository.updateDockerHost(DOCKER_HOST_NAME_1, null);
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotUpdateDockerHostWithoutName() throws Exception {
        dockerHostRepository.updateDockerHost(DOCKER_HOST_NAME_1, initNewDockerHost(null));
    }

    @Test
    public void shouldLoadAllDockerHosts() {
        assertEquals(4, dockerHostRepository.loadAll().size());
    }

    @Test
    public void shouldLoadDockerHostsByName() throws Exception {
        assertEquals(DOCKER_HOST_NAME_2, dockerHostRepository.loadByName(DOCKER_HOST_NAME_2).getName());
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotLoadDockerHostWithNullProvided() throws Exception {
        dockerHostRepository.loadByName(null);
    }

    @Test(expected = DockerHostNotFoundException.class)
    public void shouldNotLoadNotExistingDockerHost() throws Exception {
        dockerHostRepository.loadByName("WRONG-DOCKER-HOST-NAME");
    }

    @Test
    public void shouldLoadPrefferedDockerHost() throws Exception {
        assertEquals(dockerHostRepository.loadByName(PREFERRED_DOCKER_HOST_NAME), dockerHostRepository.loadPreferredDockerHost());
    }

    @Test
    public void shouldNotLoadPrefferedDockerHost() throws Exception {
        final DockerHost preferredDockerHost = dockerHostRepository.loadByName(PREFERRED_DOCKER_HOST_NAME);
        dockerHostRepository.removeDockerHost(PREFERRED_DOCKER_HOST_NAME);
        try {
            dockerHostRepository.loadPreferredDockerHost();
            assertTrue(false);
        } catch (DockerHostNotFoundException ex) {
            dockerHostRepository.addDockerHost(preferredDockerHost);
            assertTrue(true);
        }
    }

    private DockerHost initNewDockerHost(String hostName) throws Exception {
        return new DockerHost(
                hostName,
                InetAddress.getByName("192.168.0.1"),
                9999,
                InetAddress.getByName("192.168.0.1"),
                "eth0",
                "eth1",
                InetAddress.getByName("192.168.1.1"),
                "/home/mgmt/ansible/volumes",
                false);
    }
}
