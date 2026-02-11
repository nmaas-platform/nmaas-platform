package net.geant.nmaas.dcn.deployment.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.api.dto.domains.CustomerNetworkView;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

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
    private Integer maskLength;

    public static CustomerNetwork of(CustomerNetworkView network) {
        Validate.isTrue(network.maskLength() >= 0 && network.maskLength() <= 32, "Invalid mask");
        return new CustomerNetwork(network.id(), Objects.requireNonNull(network.customerIp(), "IP address must be specified"), network.maskLength());
    }

}
