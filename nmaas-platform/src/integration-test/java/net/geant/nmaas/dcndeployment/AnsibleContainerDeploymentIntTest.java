package net.geant.nmaas.dcndeployment;

import net.geant.nmaas.dcndeployment.repository.DcnRepository;
import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHostNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnsibleContainerDeploymentIntTest {

    String uniqueServiceName = "company1-client1-nmaas-ansible-239487523809475";

    @Autowired
    private DcnDeploymentCoordinator coordinator;

    @Test
    public void shouldDeployDefaultContainer() throws DockerHostNotFoundException, DcnRepository.DcnNotFoundException {
        coordinator.deploy(uniqueServiceName, VpnConfig.defaultVpn());
    }

}
