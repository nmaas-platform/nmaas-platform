package net.geant.nmaas.nmservice.deployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.KubernetesApiConnector;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.exceptions.KubernetesClusterCheckException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Set of integration tests verifying correct communication with real Kubernetes REST API.
 * Note: All tests must be ignored.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("kubernetes")
public class KubernetesApiConnectorTest {

    @Autowired
    private KubernetesApiConnector connector;

    @Ignore
    @Test
    public void shouldCheckCluster() throws KubernetesClusterCheckException {
        connector.checkClusterStatusAndPrerequisites();
    }

}
