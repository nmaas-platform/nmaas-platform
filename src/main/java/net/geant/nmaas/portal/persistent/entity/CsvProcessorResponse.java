package net.geant.nmaas.portal.persistent.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class CsvProcessorResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean successful;
    private Boolean created;
    @ElementCollection
    @CollectionTable(name = "details",
            joinColumns = {@JoinColumn(name = "id", referencedColumnName = "id")})
    @MapKeyColumn(name = "bulk_details")
    private Map<String, String> details;
}
