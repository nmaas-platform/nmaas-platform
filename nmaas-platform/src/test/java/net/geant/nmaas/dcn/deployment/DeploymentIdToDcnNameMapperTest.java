package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DeploymentIdToDcnNameMapperTest {

    @Autowired
    private DeploymentIdToDcnNameMapper mapper;

    @Test
    public void shouldStoreAndLoadSimpleMapping() throws DeploymentIdToDcnNameMapper.EntryNotFoundException {
        Identifier deploymentId = Identifier.newInstance("deploymentId");
        String dcnName = "dcnName";
        mapper.storeMapping(deploymentId, dcnName);
        assertThat(mapper.deploymentId("dcnName"), equalTo(deploymentId));
        assertThat(mapper.dcnName(Identifier.newInstance("deploymentId")), equalTo(dcnName));
    }

    @Test(expected = DeploymentIdToDcnNameMapper.EntryNotFoundException.class)
    public void shouldThrowExceptionOnMissingMappingForDcnName() throws DeploymentIdToDcnNameMapper.EntryNotFoundException {
        Identifier deploymentId = Identifier.newInstance("deploymentId");
        String dcnName = "dcnName";
        mapper.storeMapping(deploymentId, dcnName);
        mapper.deploymentId("wrongdcnname");
    }

    @Test(expected = DeploymentIdToDcnNameMapper.EntryNotFoundException.class)
    public void shouldThrowExceptionOnMissingMappingForDeploymentIdentifier() throws DeploymentIdToDcnNameMapper.EntryNotFoundException {
        Identifier deploymentId = Identifier.newInstance("deploymentId");
        String dcnName = "dcnName";
        mapper.storeMapping(deploymentId, dcnName);
        mapper.dcnName(Identifier.newInstance("wrongidentifiervalue"));
    }

}
