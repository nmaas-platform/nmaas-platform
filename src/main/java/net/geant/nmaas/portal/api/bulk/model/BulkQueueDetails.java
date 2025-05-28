package net.geant.nmaas.portal.api.bulk.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BulkQueueDetails {

    private long jobInProcess;
    private long jobInProcessId;
    private long jobInQueue;
    private long jobDone;
    private long bulkJobInQueue;

}
