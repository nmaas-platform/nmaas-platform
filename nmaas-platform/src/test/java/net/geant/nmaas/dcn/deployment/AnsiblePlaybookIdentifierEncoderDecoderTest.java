package net.geant.nmaas.dcn.deployment;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.decode;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.encodeForClientSideRouter;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.encodeForCloudSideRouter;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.wasEncodedForClientSideRouter;
import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookIdentifierConverter.wasEncodedForCloudSideRouter;
import net.geant.nmaas.orchestration.entities.Identifier;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class AnsiblePlaybookIdentifierEncoderDecoderTest {

    private Identifier appDeploymentId;

    private String originalDcnName;

    @Before
    public void setup() {
        appDeploymentId = Identifier.newInstance("appDeploymentId");
        originalDcnName = appDeploymentId.value();
    }

    @Test
    public void shouldEncodeAndDecodePlaybookIdForClientSide() throws AnsiblePlaybookIdentifierConverter.AnsiblePlaybookIdentifierConverterException {
        final String encodedPlaybookId = encodeForClientSideRouter(originalDcnName);
        assertThat(wasEncodedForClientSideRouter(encodedPlaybookId), is(true));
        assertThat(wasEncodedForCloudSideRouter(encodedPlaybookId), is(false));
        final String decodedDcnName = decode(encodedPlaybookId);
        assertThat(decodedDcnName, equalTo(originalDcnName));
    }

    @Test
    public void shouldEncodeAndDecodePlaybookIdForCloudSide() throws AnsiblePlaybookIdentifierConverter.AnsiblePlaybookIdentifierConverterException {
        final String encodedPlaybookId = encodeForCloudSideRouter(originalDcnName);
        assertThat(wasEncodedForClientSideRouter(encodedPlaybookId), is(false));
        assertThat(wasEncodedForCloudSideRouter(encodedPlaybookId), is(true));
        final String decodedDcnName = decode(encodedPlaybookId);
        assertThat(decodedDcnName, equalTo(originalDcnName));
    }

    @Test(expected = AnsiblePlaybookIdentifierConverter.AnsiblePlaybookIdentifierConverterException.class)
    public void shouldThrowExceptionOnMalformedEncodedPlaybookId() throws AnsiblePlaybookIdentifierConverter.AnsiblePlaybookIdentifierConverterException {
        final String encodedPlaybookId = "YnVn" + encodeForCloudSideRouter(originalDcnName);
        decode(encodedPlaybookId);
    }

    @Test
    public void shouldDecodeStringsToKnownPlainDcnName() throws AnsiblePlaybookIdentifierConverter.AnsiblePlaybookIdentifierConverterException {
        String plainDcnName = "testClientId-oxidizedApplicationId";
        assertThat(decode("Q0xJRU5ULVJPVVRFUnRlc3RDbGllbnRJZC1veGlkaXplZEFwcGxpY2F0aW9uSWQ="), equalTo(plainDcnName));
        assertThat(decode("Q0xPVUQtUk9VVEVSdGVzdENsaWVudElkLW94aWRpemVkQXBwbGljYXRpb25JZA=="), equalTo(plainDcnName));
    }

}
