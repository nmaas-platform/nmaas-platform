package net.geant.nmaas.dcn.deployment.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DomainDcnDetails implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String domainCodename;

    private boolean dcnConfigured = false;

    private DcnDeploymentType dcnDeploymentType = DcnDeploymentType.MANUAL;

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private List<CustomerNetwork> customerNetworks = new ArrayList<>();

}
