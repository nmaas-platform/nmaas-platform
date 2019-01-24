package net.geant.nmaas.notifications;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.orchestration.api.model.AppDeploymentView;
import net.geant.nmaas.portal.api.domain.User;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@Setter
public class MailAttributes implements Serializable {

    private List<User> addressees;

    @NotNull
    private MailType mailType;

    private String otherAttribute;

    private AppDeploymentView appDeploymentView;
}
