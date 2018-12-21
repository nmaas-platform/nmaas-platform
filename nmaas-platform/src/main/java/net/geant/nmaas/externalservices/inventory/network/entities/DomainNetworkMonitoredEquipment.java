package net.geant.nmaas.externalservices.inventory.network.entities;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name="domain_network_monitored_equipment")
public class DomainNetworkMonitoredEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @ElementCollection
    private List<String> addresses = new ArrayList<>();

    @ElementCollection
    private List<String> networks = new ArrayList<>();
}
