package net.geant.nmaas.externalservices.inventory.kubernetes.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class KClusterAttachPointView {

    private Long id;

    private String routerName;

    private String routerId;

    private String routerInterfaceName;
}
