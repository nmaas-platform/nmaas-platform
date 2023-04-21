package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BulkDeploymentRecordViewS {

    private Long id;
    private UserViewMinimal creator;
    private OffsetDateTime date;
    private BulkDeploymentState state;
}
