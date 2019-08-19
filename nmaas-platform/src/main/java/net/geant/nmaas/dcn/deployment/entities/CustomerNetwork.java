package net.geant.nmaas.dcn.deployment.entities;

import static com.google.common.base.Preconditions.checkArgument;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class CustomerNetwork implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private InetAddress customerIp;

    @Column(nullable = false)
    private int maskLength;

    public CustomerNetwork(Long id, InetAddress customerIp, int maskLength){
        checkArgument(maskLength < 0 || maskLength > 24, "Invalid mask");
        this.id = id;
        this.customerIp = Objects.requireNonNull(customerIp, "IP address must be specified");
        this.maskLength = maskLength;
    }
}
