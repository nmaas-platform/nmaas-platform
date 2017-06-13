package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.api.AnsiblePlaybookStatus;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsiblePlaybookStatusHandlingTest {

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

}
