package net.geant.nmaas.externalservices.inventory.dockerhosts.entities;

import net.geant.nmaas.orchestration.entities.Identifier;

import javax.persistence.*;

/**
 * Represents an assignment of a single numeric value (that may represent for instance port number, VLAN number or
 * last octet of an IP address) to an owner being the identifier of a deployment or client.
 *
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
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

    public NumberAssignment() {
    }

    public NumberAssignment(Integer number, Identifier ownerId) {
        this.number = number;
        this.ownerId = ownerId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Identifier getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Identifier ownerId) {
        this.ownerId = ownerId;
    }
}
