package net.geant.nmaas.portal.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEventDto {

    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String targetUrl;
    @NotNull
    private WebhookEventType eventType;
    private String tokenValue;
    @Pattern(regexp = "^(Authorization|X-.*)?$", message = "Authorization header must be either 'Authorization' or start with 'X-'")
    private String authorizationHeader;
    private DomainBase domain;

    public WebhookEventDto(Long id, String name, String targetUrl, WebhookEventType eventType) {
        this.id = id;
        this.name = name;
        this.targetUrl = targetUrl;
        this.eventType = eventType;
    }
}
