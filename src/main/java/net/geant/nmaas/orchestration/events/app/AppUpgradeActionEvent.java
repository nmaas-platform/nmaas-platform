package net.geant.nmaas.orchestration.events.app;

import lombok.Getter;
import net.geant.nmaas.orchestration.AppUpgradeMode;
import net.geant.nmaas.orchestration.Identifier;

public class AppUpgradeActionEvent extends AppBaseEvent {

    @Getter
    private final Identifier applicationId;

    @Getter
    private final AppUpgradeMode appUpgradeMode;

    @Getter
    private String username;

    public AppUpgradeActionEvent(Object source, Identifier deploymentId, Identifier applicationId, AppUpgradeMode appUpgradeMode) {
        super(source, deploymentId);
        this.applicationId = applicationId;
        this.appUpgradeMode = appUpgradeMode;
    }

    public AppUpgradeActionEvent(
            Object source,
            Identifier deploymentId,
            Identifier applicationId,
            AppUpgradeMode appUpgradeMode,
            String username
    ) {
        super(source, deploymentId);
        this.applicationId = applicationId;
        this.appUpgradeMode = appUpgradeMode;
        this.username = username;
    }

}
