package net.geant.nmaas.portal.api.logs;

import lombok.Value;

import java.util.List;

@Value
public class PodLogs {

    String name;
    List<String> lines;

}
