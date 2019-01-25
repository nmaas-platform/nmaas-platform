package net.geant.nmaas.notifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.domain.User;

@Builder
@Getter
@Setter
public class MailAttributes {

    private List<User> addressees;

    @NotNull
    private MailType mailType;

    @Builder.Default
    private Map<String, String> otherAttributes = new HashMap<>();
}
