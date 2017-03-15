package net.geant.nmaas.dcndeployment;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.dockerengine.DockerContainerSpec;
import org.junit.Before;
import org.junit.Test;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsiblePlaybookIdentifierEncoderDecoderTest {

    private String originalDcnName;

    @Before
    public void setup() {
        DockerContainerSpec serviceSpec = new DockerContainerSpec("serviceName1", null);
        serviceSpec.setClientDetails("client1", "company1");
        originalDcnName = serviceSpec.uniqueDeploymentName();
    }

    @Test
    public void shouldEncodeAndDecodeDcnNameForClientSideRouterPlaybook() throws AnsiblePlaybookIdentifierConverterException {
        final String encodedPlaybookIdForClientSideRouter = encodeForClientSideRouter(originalDcnName);
        assertThat(wasEncodedForClientSideRouter(encodedPlaybookIdForClientSideRouter), is(true));
        assertThat(wasEncodedForCloudSideRouter(encodedPlaybookIdForClientSideRouter), is(false));
        final String decodedDcnName = decode(encodedPlaybookIdForClientSideRouter);
        assertThat(decodedDcnName, equalTo(originalDcnName));
    }

    @Test
    public void shouldEncodeAndDecodeDcnNameForCloudSideRouterPlaybook() throws AnsiblePlaybookIdentifierConverterException {
        final String encodedPlaybookIdForCloudSideRouter = encodeForCloudSideRouter(originalDcnName);
        assertThat(wasEncodedForClientSideRouter(encodedPlaybookIdForCloudSideRouter), is(false));
        assertThat(wasEncodedForCloudSideRouter(encodedPlaybookIdForCloudSideRouter), is(true));
        final String decodedDcnName = decode(encodedPlaybookIdForCloudSideRouter);
        assertThat(decodedDcnName, equalTo(originalDcnName));
    }

    @Test(expected = AnsiblePlaybookIdentifierConverterException.class)
    public void shouldThrowExceptionOnMalformedEncodedIdentifier() throws AnsiblePlaybookIdentifierConverterException {
        final String malformedEncodedIdentifier = "YnVn" + encodeForCloudSideRouter(originalDcnName);
        decode(malformedEncodedIdentifier);
    }

}
