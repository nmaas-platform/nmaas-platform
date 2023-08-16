package net.geant.nmaas.portal.persistent.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.geant.nmaas.portal.api.bulk.BulkType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class BulkDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long creatorId;

    private OffsetDateTime creationDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BulkDeploymentState state;

    @Column(name = "bulk_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private BulkType type;

    @OneToMany(cascade = CascadeType.ALL)
    private List<BulkDeploymentEntry> entries = new ArrayList<>();

}
