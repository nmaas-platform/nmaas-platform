package net.geant.nmaas.portal.persistent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.geant.nmaas.portal.api.bulk.BulkType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@ToString
public class BulkDeploymentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bulk_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BulkType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BulkDeploymentState state;

    private Boolean created;

    @ElementCollection
    @Fetch(FetchMode.SELECT)
    private Map<String, String> details;

}
