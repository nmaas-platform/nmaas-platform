package net.geant.nmaas.dcndeployment.api;

import java.util.Arrays;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AnsiblePlaybookStatus {

    private String status;

    public AnsiblePlaybookStatus() {}

    public AnsiblePlaybookStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Status convertedStatus() {
        return Status.fromValue(status);
    }

    public enum Status {

        SUCCESS ("success"),
        FAILURE ("failure");

        private String value;

        Status(String value) {
            this.value = value;
        }

        public static Status fromValue(String statusValue) {
            return Arrays.stream(Status.values())
                    .filter((status) -> statusValue.equals(status.getValue()))
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("Wrong status value."));
        }

        public String getValue() {
            return value;
        }
    }
}
