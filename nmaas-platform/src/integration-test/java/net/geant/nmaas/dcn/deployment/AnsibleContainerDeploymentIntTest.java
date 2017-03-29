package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.nmservice.InvalidDeploymentIdException;
import net.geant.nmaas.orchestration.Identifier;
import org.junit.Ignore;
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

    Identifier deploymentId = new Identifier("exampledeploymentid");
    String uniqueDcnName = "company1-client1-nmaas-ansible-239487523809475";

    @Autowired
    private DcnDeploymentProvider dcnDeployment;

    @Ignore
    @Test
    public void shouldVerifyAndDeployDefaultContainer() throws InvalidDeploymentIdException {
        dcnDeployment.verifyRequest(deploymentId, new DcnSpec(uniqueDcnName));
        dcnDeployment.deployDcn(deploymentId);
    }

}
