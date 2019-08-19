package net.geant.nmaas.dcn.deployment.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
    private List<CustomerNetwork> customerNetworks;

}
