package net.geant.nmaas.dcndeployment;

import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerContainerSpec;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class DcnIdentifierEncoderDecoderTest {

    @Test
    public void shouldEncodeAndDecodeDcnId() {
        DockerContainerSpec serviceSpec = new DockerContainerSpec("serviceName1", System.nanoTime(), null);
        serviceSpec.setClientDetails("client1", "company1");
        final String originalServiceName = serviceSpec.uniqueDeploymentName();
        final String encodedServiceId = ServiceNameConverter.encode(originalServiceName);
        final String decodedServiceName = ServiceNameConverter.decode(encodedServiceId);
        assertThat(decodedServiceName, equalTo(originalServiceName));
    }

}
