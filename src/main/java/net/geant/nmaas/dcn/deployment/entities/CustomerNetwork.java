package net.geant.nmaas.dcn.deployment.entities;

import static com.google.common.base.Preconditions.checkArgument;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.api.domain.CustomerNetworkView;

@Entity
@AllArgsConstructor
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

    public static CustomerNetwork of(CustomerNetworkView network){
        checkArgument(network.getMaskLength() >= 0 && network.getMaskLength() <= 32, "Invalid mask");
        return new CustomerNetwork(network.getId(), Objects.requireNonNull(network.getCustomerIp(), "IP address must be specified"), network.getMaskLength());
    }
}
