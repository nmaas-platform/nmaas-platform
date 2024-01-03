package net.geant.nmaas.orchestration;

import lombok.Value;

import java.util.List;

@Value
public class AppComponentLogs {

    String name;
    List<String> lines;

}
