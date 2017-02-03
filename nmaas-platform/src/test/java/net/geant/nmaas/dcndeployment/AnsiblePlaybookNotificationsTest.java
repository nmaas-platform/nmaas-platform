package net.geant.nmaas.dcndeployment;

import net.geant.nmaas.dcndeployment.api.AnsiblePlaybookStatus;
import net.geant.nmaas.servicedeployment.orchestrators.dockerengine.DockerContainerSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnsiblePlaybookNotificationsTest {

    @Test
    public void shouldReturnCorrectStatus() {
        AnsiblePlaybookStatus status = new AnsiblePlaybookStatus();
        status.setStatus("success");
        assertThat(status.convertedStatus(), equalTo(AnsiblePlaybookStatus.Status.SUCCESS));
        status.setStatus("failure");
        assertThat(status.convertedStatus(), equalTo(AnsiblePlaybookStatus.Status.FAILURE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionOnInvalidStatusValue() {
        AnsiblePlaybookStatus status = new AnsiblePlaybookStatus();
        status.setStatus("invalidvalue");
        assertThat(status.convertedStatus(), equalTo(AnsiblePlaybookStatus.Status.SUCCESS));
    }

    @Test
    public void shouldEncodeAndDecodeServiceId() {
        DockerContainerSpec serviceSpec = new DockerContainerSpec("serviceName1", System.nanoTime(), null);
        serviceSpec.setClientDetails("client1", "company1");
        final String originalServiceName = serviceSpec.uniqueDeploymentName();
        final String encodedServiceId = ServiceNameConverter.encode(originalServiceName);
        final String decodedServiceName = ServiceNameConverter.decode(encodedServiceId);
        assertThat(decodedServiceName, equalTo(originalServiceName));
    }

}
