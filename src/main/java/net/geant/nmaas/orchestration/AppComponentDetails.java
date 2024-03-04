package net.geant.nmaas.orchestration;

import lombok.Value;

import java.util.List;

@Value
public class AppComponentDetails {

    String name;
    String displayName;
    List<String> subComponents;

}
