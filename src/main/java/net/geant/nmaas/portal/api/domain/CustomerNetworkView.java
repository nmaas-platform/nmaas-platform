package net.geant.nmaas.portal.api.domain;

import java.net.InetAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerNetworkView {
    private Long id;
    private InetAddress customerIp;
    private int maskLength;
}
