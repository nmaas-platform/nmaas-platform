package net.geant.nmaas.externalservices.inventory.dockerhosts;

import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostAlreadyExistsException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostInvalidException;
import net.geant.nmaas.externalservices.inventory.dockerhosts.exceptions.DockerHostNotFoundException;
import net.geant.nmaas.nmservice.deployment.entities.DockerHost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jakub Gutkowski <jgutkow@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerHostRepositoryManagerTest {

    @Autowired
    private DockerHostRepositoryManager dockerHostRepositoryManager;

    private final static String TEST_DOCKER_HOST_NAME = "TEST-DOCKER-HOST-1";
    private final static String PREFERRED_DOCKER_HOST_NAME = "GN4-DOCKER-1";
    private final static String EXISTING_DOCKER_HOST_NAME = "GN4-DOCKER-1";
    private final static String PREFERRED_DOCKER_HOST_FOR_DOCKER_COMPOSE_NAME = "GN4-DOCKER-2";
    private final static String VOLUME_PATH = "/new/path";

    @Before
    public void init() {
        DockerHostRepositoryInit.addDefaultDockerHost(dockerHostRepositoryManager);
    }

    @After
    public void clean() {
        DockerHostRepositoryInit.removeDefaultDockerHost(dockerHostRepositoryManager);
    }

    @Test
    public void shouldAddDockerHost() throws Exception {
        dockerHostRepositoryManager.addDockerHost(initNewDockerHost(TEST_DOCKER_HOST_NAME));
        assertEquals(initNewDockerHost(TEST_DOCKER_HOST_NAME), dockerHostRepositoryManager.loadByName(TEST_DOCKER_HOST_NAME));
        dockerHostRepositoryManager.removeDockerHost(TEST_DOCKER_HOST_NAME);
    }

    @Test(expected = DockerHostAlreadyExistsException.class)
    public void shouldNotAddTheSameDockerHost() throws Exception {
        dockerHostRepositoryManager.addDockerHost(initNewDockerHost(EXISTING_DOCKER_HOST_NAME));
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotAddDockerHostWithoutName() throws Exception {
        dockerHostRepositoryManager.addDockerHost(initNewDockerHost(null));
    }

    @Test(expected = DockerHostNotFoundException.class)
    public void shouldRemoveDockerHost() throws Exception {
        dockerHostRepositoryManager.addDockerHost(initNewDockerHost(TEST_DOCKER_HOST_NAME));
        dockerHostRepositoryManager.removeDockerHost(TEST_DOCKER_HOST_NAME);
        dockerHostRepositoryManager.loadByName(TEST_DOCKER_HOST_NAME);
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotRemoveDockerHostWithoutName() throws Exception {
        dockerHostRepositoryManager.removeDockerHost(null);
    }

    @Test
    public void shouldUpdateDockerHost() throws Exception {
        dockerHostRepositoryManager.addDockerHost(initNewDockerHost(TEST_DOCKER_HOST_NAME));
        assertEquals("/home/mgmt/volumes", dockerHostRepositoryManager.loadByName(TEST_DOCKER_HOST_NAME).getVolumesPath());
        DockerHost dockerHost = initNewDockerHost(TEST_DOCKER_HOST_NAME);
        dockerHost.setVolumesPath(VOLUME_PATH);
        dockerHostRepositoryManager.updateDockerHost(TEST_DOCKER_HOST_NAME, dockerHost);
        assertEquals(VOLUME_PATH, dockerHostRepositoryManager.loadByName(TEST_DOCKER_HOST_NAME).getVolumesPath());
        dockerHostRepositoryManager.removeDockerHost(TEST_DOCKER_HOST_NAME);
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotUpdateDockerHostWhenNameIsNull() throws Exception {
        dockerHostRepositoryManager.updateDockerHost(null, initNewDockerHost(TEST_DOCKER_HOST_NAME));
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotUpdateNullDockerHost() throws Exception {
        dockerHostRepositoryManager.updateDockerHost(TEST_DOCKER_HOST_NAME, null);
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotUpdateDockerHostWithoutName() throws Exception {
        dockerHostRepositoryManager.updateDockerHost(TEST_DOCKER_HOST_NAME, initNewDockerHost(null));
    }

    @Test
    public void shouldLoadAllDockerHosts() {
        assertEquals(3, dockerHostRepositoryManager.loadAll().size());
    }

    @Test(expected = DockerHostInvalidException.class)
    public void shouldNotLoadDockerHostWithNullProvided() throws Exception {
        dockerHostRepositoryManager.loadByName(null);
    }

    @Test(expected = DockerHostNotFoundException.class)
    public void shouldNotLoadNotExistingDockerHost() throws Exception {
        dockerHostRepositoryManager.loadByName("WRONG-DOCKER-HOST-NAME");
    }

    @Test
    public void shouldLoadPreferredDockerHost() throws Exception {
        assertEquals(
                dockerHostRepositoryManager.loadByName(PREFERRED_DOCKER_HOST_NAME),
                dockerHostRepositoryManager.loadPreferredDockerHost());
    }

    @Test
    public void shouldNotLoadPreferredDockerHost() throws Exception {
        final DockerHost preferredDockerHost = dockerHostRepositoryManager.loadByName(PREFERRED_DOCKER_HOST_NAME);
        dockerHostRepositoryManager.removeDockerHost(PREFERRED_DOCKER_HOST_NAME);
        try {
            dockerHostRepositoryManager.loadPreferredDockerHost();
            assertTrue(false);
        } catch (DockerHostNotFoundException ex) {
            dockerHostRepositoryManager.addDockerHost(preferredDockerHost);
            assertTrue(true);
        }
    }

    @Test
    public void shouldLoadPreferredDockerHostForDockerCompose() throws Exception {
        assertEquals(
                dockerHostRepositoryManager.loadByName(PREFERRED_DOCKER_HOST_FOR_DOCKER_COMPOSE_NAME),
                dockerHostRepositoryManager.loadPreferredDockerHostForDockerCompose());
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
                "/home/mgmt/scripts",
                "/home/mgmt/volumes",
                false);
    }
}
