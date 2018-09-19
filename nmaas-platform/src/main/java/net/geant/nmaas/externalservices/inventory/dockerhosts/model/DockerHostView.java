package net.geant.nmaas.externalservices.inventory.dockerhosts.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DockerHostView {

    private String name;
    private String apiIpAddress;
    private Integer apiPort;
    private String publicIpAddress;
    private boolean preferred;

}
