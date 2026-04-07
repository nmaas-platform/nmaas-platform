package net.geant.nmaas.notifications;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.api.dto.users.UserDto;
import net.geant.nmaas.notifications.templates.MailType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Builder
@Getter
@Setter
@NoArgsConstructor
public class MailAttributes implements Serializable {

    private List<UserDto> addresses;
    private MailType mailType;
    @Builder.Default
    private Map<String, Object> otherAttributes = new HashMap<>();

    @Builder
    @SuppressWarnings("unused")
    private MailAttributes(List<UserDto> addresses, MailType mailType, Map<String, Object> otherAttributes) {
        this.addresses = addresses;
        this.mailType = mailType;
        this.otherAttributes = Optional.ofNullable(otherAttributes).orElse(this.otherAttributes);
    }

}
