package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import java.net.InetAddress;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class KClusterApiView {
    private Long id;

    private InetAddress restApiHostAddress;

    private Integer restApiPort;

    private boolean useKClusterApi = false;
}
