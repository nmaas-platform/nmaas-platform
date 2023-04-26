package net.geant.nmaas.portal.persistent.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.api.bulk.BulkType;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BulkDeployment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long creatorId;
    private Date startDate;
    private BulkDeploymentState status;
    private BulkType type;

    @OneToMany
    private List<CsvProcessorResponse> csvResponses;

}
