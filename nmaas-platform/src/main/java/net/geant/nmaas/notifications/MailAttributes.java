package net.geant.nmaas.notifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.api.domain.User;

@Getter
@Setter
@NoArgsConstructor
public class MailAttributes {

    private List<User> addressees;

    private MailType mailType;

    private Map<String, String> otherAttributes = new HashMap<>();

    @Builder
    @SuppressWarnings("unused")
    private MailAttributes(List<User> addressees, MailType mailType, Map<String, String> otherAttributes){
        this.addressees = addressees;
        this.mailType = mailType;
        this.otherAttributes = Optional.ofNullable(otherAttributes).orElse(this.otherAttributes);
    }
}
