package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.AppUpgradeMode;
import net.geant.nmaas.orchestration.Identifier;

@Getter
public class AppUpgradeCompleteEvent extends AppBaseEvent {

    private final Identifier previousApplicationId;
    private final Identifier targetApplicationId;
    private final AppUpgradeMode appUpgradeMode;

    public AppUpgradeCompleteEvent(Object source,
                                   Identifier deploymentId,
                                   Identifier previousApplicationId,
                                   Identifier targetApplicationId,
                                   AppUpgradeMode appUpgradeMode) {
        super(source, deploymentId);
        this.previousApplicationId = previousApplicationId;
        this.targetApplicationId = targetApplicationId;
        this.appUpgradeMode = appUpgradeMode;
    }

}
