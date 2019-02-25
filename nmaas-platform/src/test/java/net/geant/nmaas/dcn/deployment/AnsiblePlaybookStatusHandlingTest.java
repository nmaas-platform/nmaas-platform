package net.geant.nmaas.dcn.deployment;

import net.geant.nmaas.dcn.deployment.api.model.AnsiblePlaybookStatus;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AnsiblePlaybookStatusHandlingTest {

    @Test
    public void shouldReturnCorrectStatus() {
        AnsiblePlaybookStatus status = new AnsiblePlaybookStatus();
        status.setStatus("success");
        assertThat(status.convertedStatus(), equalTo(AnsiblePlaybookStatus.Status.SUCCESS));
        status.setStatus("failure");
        assertThat(status.convertedStatus(), equalTo(AnsiblePlaybookStatus.Status.FAILURE));
    }

    @Test
    public void shouldThrowExceptionOnInvalidStatusValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            AnsiblePlaybookStatus status = new AnsiblePlaybookStatus();
            status.setStatus("invalidvalue");
            assertThat(status.convertedStatus(), equalTo(AnsiblePlaybookStatus.Status.SUCCESS));
        });
    }

}
