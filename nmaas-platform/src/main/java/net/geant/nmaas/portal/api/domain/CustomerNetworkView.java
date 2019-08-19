package net.geant.nmaas.portal.api.domain;

import static com.google.common.base.Preconditions.checkArgument;
import java.net.InetAddress;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CustomerNetworkView {
    private Long id;
    private InetAddress customerIp;
    private int maskLength;
    public CustomerNetworkView(Long id, InetAddress customerIp, int maskLength){
        checkArgument(maskLength < 0 || maskLength > 24, "Invalid mask");
        this.id = id;
        this.customerIp = Objects.requireNonNull(customerIp, "IP address must be specified");
        this.maskLength = maskLength;
    }
}
