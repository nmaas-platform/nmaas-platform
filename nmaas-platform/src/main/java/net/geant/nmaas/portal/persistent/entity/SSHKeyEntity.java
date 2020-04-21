package net.geant.nmaas.portal.persistent.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SSHKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String key;
    // base64 encoded SHA256
    private String fingerprint;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private User owner;

    public SSHKeyEntity(User owner, String name, String key) {
        this.owner = owner;
        this.name = name;
        this.key = key;
        this.fingerprint = "fingerprint"; // TODO calculate fingerprint
    }

}
