package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;

@Getter
@Setter
public class KubernetesClusterView {

    private Long id;
    private InetAddress helmHostAddress;
    private InetAddress restApiHostAddress;
    private int restApiPort;
}
