package net.geant.nmaas.externalservices.inventory.network.model;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
class DomainNetworkMonitoredEquipmentView {

    private Long id;

    private List<String> addresses = new ArrayList<>();

    private List<String> networks = new ArrayList<>();
}
