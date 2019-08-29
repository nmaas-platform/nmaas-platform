package net.geant.nmaas.notifications;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.notifications.templates.MailType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.geant.nmaas.portal.api.domain.UserView;

@Builder
@Getter
@Setter
@NoArgsConstructor
public class MailAttributes implements Serializable {

    private List<UserView> addressees;

    private MailType mailType;

    @Builder.Default
    private Map<String, String> otherAttributes = new HashMap<>();

    @Builder
    @SuppressWarnings("unused")
    private MailAttributes(List<UserView> addressees, MailType mailType, Map<String, String> otherAttributes){
        this.addressees = addressees;
        this.mailType = mailType;
        this.otherAttributes = Optional.ofNullable(otherAttributes).orElse(this.otherAttributes);
    }
}
