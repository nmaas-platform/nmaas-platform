package net.geant.nmaas.orchestration;

import lombok.Value;

@Value(staticConstructor = "of")
public class AppConfigRepositoryAccessDetails {

    String cloneUrl;

}
