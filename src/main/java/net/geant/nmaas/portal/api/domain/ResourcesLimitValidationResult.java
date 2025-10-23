package net.geant.nmaas.portal.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ResourcesLimitValidationResult {
    private boolean accepted;
    private List<RejectionReason> reasons = new ArrayList<>();
}


