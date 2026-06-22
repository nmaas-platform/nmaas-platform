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
public class AppDeploymentHistoryDto {
    private Date timestamp;
    private String previousState;
    private String currentState;
    private String initiator;
}
