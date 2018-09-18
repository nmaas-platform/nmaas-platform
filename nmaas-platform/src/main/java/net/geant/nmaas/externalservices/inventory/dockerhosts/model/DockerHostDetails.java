package net.geant.nmaas.externalservices.inventory.dockerhosts.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DockerHostDetails {

    private String name;
    private String apiIpAddress;
    private Integer apiPort;
    private String publicIpAddress;
    private String accessInterfaceName;
    private String dataInterfaceName;
    private String baseDataNetworkAddress;
    private String workingPath;
    private String volumesPath;
    private boolean preferred;

}
