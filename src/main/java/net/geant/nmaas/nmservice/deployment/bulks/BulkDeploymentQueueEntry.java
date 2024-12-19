package net.geant.nmaas.nmservice.deployment.bulks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.orchestration.Identifier;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkDeploymentQueueEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Identifier deploymentId;

    private Long bulkEntryId;

    @JsonProperty("appConfigurationJson")
    private String appConfigurationJson;

    @Enumerated(EnumType.STRING)
    private QueryEntryState state;

    public enum QueryEntryState {
        WAITING,
        IN_PROGRESS
    }

}
