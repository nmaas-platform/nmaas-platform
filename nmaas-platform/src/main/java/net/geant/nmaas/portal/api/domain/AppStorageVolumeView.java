package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppStorageVolumeView {

    private Boolean main;

    private Integer defaultStorageSpace;

    private Map<String, String> deployParameters;

}
