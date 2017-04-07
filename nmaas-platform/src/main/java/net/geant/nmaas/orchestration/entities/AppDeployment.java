package net.geant.nmaas.orchestration.entities;

import javax.persistence.*;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Entity
@Table(name="app_deployment")
public class AppDeployment {

    @Id
    @GeneratedValue( strategy = GenerationType.AUTO )
    @Column(name="id")
    private Long id;

    private Identifier deploymentId;

    private Identifier clientId;

    private Identifier applicationId;

    private AppDeploymentState state;

}
