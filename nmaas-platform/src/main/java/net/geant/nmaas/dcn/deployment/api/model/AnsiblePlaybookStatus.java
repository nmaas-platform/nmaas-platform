package net.geant.nmaas.dcn.deployment.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AnsiblePlaybookStatus {

    private String status;

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
