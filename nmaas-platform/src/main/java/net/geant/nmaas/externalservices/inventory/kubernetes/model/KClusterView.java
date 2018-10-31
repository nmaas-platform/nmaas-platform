package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KClusterView {

    private Long id;

    private KClusterApiView api;

    private KClusterIngressView ingress;

    private KClusterDeploymentView deployment;

    private KClusterAttachPointView attachPoint;

    private List<KClusterExtNetworkView> externalNetworks = new ArrayList<>();

}
