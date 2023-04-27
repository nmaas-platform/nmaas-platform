package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BulkDeploymentViewS {

    private Long id;
    private UserViewMinimal creator;
    private OffsetDateTime creationDate;
    private BulkDeploymentState state;
    private BulkType type;

}
