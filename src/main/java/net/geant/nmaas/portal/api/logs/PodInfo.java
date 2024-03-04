package net.geant.nmaas.portal.api.logs;

import lombok.Value;

import java.util.List;

@Value
public class PodInfo {

    String name;
    String displayName;
    List<String> containers;
}
