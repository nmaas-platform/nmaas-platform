package net.geant.nmaas.orchestration.api.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppDeploymentHistoryView {
    private Date timestamp;
    private String previousState;
    private String currentState;
}
