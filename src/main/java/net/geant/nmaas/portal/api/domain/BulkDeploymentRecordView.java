package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.api.bulk.CsvProcessorResponse;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BulkDeploymentRecordView extends BulkDeploymentRecordViewS {

    private List<CsvProcessorResponse> processorResponses;
}
