package net.geant.nmaas.externalservices.inventory.gitlab.model;

import java.net.InetAddress;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Getter
@Setter
public class KubernetesClusterView {

    private Long id;
    private InetAddress helmHostAddress;
    private InetAddress restApiHostAddress;
    private int restApiPort;
}
