package net.geant.nmaas.externalservices.inventory.network.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DockerHostAttachPointView {

    private Long id;

    private String dockerHostName;

    private String routerName;

    private String routerId;

    private String routerInterfaceName;
}
