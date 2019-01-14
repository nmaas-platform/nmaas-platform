package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KClusterViewComplete {

    private Long id;

    private KClusterIngressView ingress;

    private KClusterDeploymentView deployment;

    private KClusterAttachPointView attachPoint;

    private List<KClusterExtNetworkView> externalNetworks = new ArrayList<>();

}
