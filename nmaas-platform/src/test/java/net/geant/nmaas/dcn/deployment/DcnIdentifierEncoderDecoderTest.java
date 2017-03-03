package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.DcnIdentifierConverter;
import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnIdentifierEncoderDecoderTest {

    @Test
    public void shouldEncodeAndDecodeDcnId() {
        DockerContainerSpec serviceSpec = new DockerContainerSpec("serviceName1", null);
        serviceSpec.setClientDetails("client1", "company1");
        final String originalServiceName = serviceSpec.uniqueDeploymentName();
        final String encodedServiceId = DcnIdentifierConverter.encode(originalServiceName);
        final String decodedServiceName = DcnIdentifierConverter.decode(encodedServiceId);
        assertThat(decodedServiceName, equalTo(originalServiceName));
    }

}
