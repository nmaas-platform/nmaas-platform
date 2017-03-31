package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.externalservices.inventory.dockerhosts.DockerHost;
import net.geant.nmaas.nmservice.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnsibleContainerDeploymentIntTest {

    Identifier deploymentId = new Identifier("exampledeploymentid");
    String uniqueDcnName = "company1-client1-nmaas-ansible-239487523809475";

    @Autowired
    private DcnDeploymentCoordinator dcnDeployment;

    @Ignore
    @Test
    public void shouldVerifyAndDeployDefaultContainer() throws InvalidDeploymentIdException {
        dcnDeployment.verifyRequest(deploymentId, new DcnSpec(uniqueDcnName));
        dcnDeployment.deployDcn(deploymentId);
    }

    @Ignore
    @Test
    public void shouldRemoveOldContainersFromAnsibleHost() throws UnknownHostException {
        dcnDeployment.removeOldAnsiblePlaybookContainers(defaultAnsibleHost());
    }

    private DockerHost defaultAnsibleHost() throws UnknownHostException {
        return new DockerHost(
                "ANSIBLE_DOCKER_HOST",
                InetAddress.getByName("10.134.250.6"),
                2375,
                InetAddress.getByName("10.134.250.6"),
                "eth0",
                "eth1",
                InetAddress.getByName("10.16.0.0"),
                "/home/mgmt/ansible/volumes",
                false);
    }

}
