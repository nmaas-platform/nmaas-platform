package net.geant.nmaas.nmservice.deployment.containerorchestrators.dockercompose.entities;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="docker_compose_service_component")
public class DockerComposeServiceComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String deploymentName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String ipAddressOfContainer;
}
