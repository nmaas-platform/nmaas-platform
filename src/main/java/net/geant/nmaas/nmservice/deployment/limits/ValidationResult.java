package net.geant.nmaas.nmservice.deployment.limits;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ValidationResult {

    private boolean accepted;
    private List<RejectionReason> reasons = new ArrayList<>();

    public ValidationResult(boolean accepted) {
        this.accepted = accepted;
    }

    public static ValidationResult accepted() {
        return new ValidationResult(true);
    }

}


