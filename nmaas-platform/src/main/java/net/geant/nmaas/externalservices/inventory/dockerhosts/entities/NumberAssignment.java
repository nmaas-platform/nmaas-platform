package net.geant.nmaas.externalservices.inventory.dockerhosts.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.orchestration.entities.Identifier;


/**
 * Represents an assignment of a single numeric value (that may represent for instance port number, VLAN number or
 * last octet of an IP address) to an owner being the identifier of a deployment or client.
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="number_assignment")
public class NumberAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private Identifier ownerId;

    public NumberAssignment(Integer number, Identifier ownerId) {
        this.number = number;
        this.ownerId = ownerId;
    }
}
