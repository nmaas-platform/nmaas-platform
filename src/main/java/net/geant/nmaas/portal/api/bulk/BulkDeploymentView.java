package net.geant.nmaas.portal.api.bulk;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BulkDeploymentView extends BulkDeploymentViewS {

    private List<BulkDeploymentEntryView> entries;

}
