package net.geant.nmaas.portal.persistent.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class SSHKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition="TEXT")
    private String key;
    // base64 encoded SHA256
    @EqualsAndHashCode.Exclude
    private String fingerprint;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @EqualsAndHashCode.Exclude
    private User owner;

    public SSHKeyEntity(User owner, String name, String key) {
        this.owner = owner;
        this.name = name;
        this.key = key;
        this.fingerprint = "fingerprint";

        // generate fingerprint
        // https://stackoverflow.com/questions/51059782/how-to-calculate-fingerprint-from-ssh-rsa-public-key-in-java
        String temp = "";
        String[] elements = key.split(" ");
        if(elements.length>1) {
            temp = elements[1];
        } else {
            temp = elements[0];
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest(Base64.getDecoder().decode(temp));
            this.fingerprint = Base64.getEncoder().encodeToString(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

}
