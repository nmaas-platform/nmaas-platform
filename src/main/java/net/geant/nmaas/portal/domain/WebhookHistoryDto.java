package net.geant.nmaas.portal.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookHistoryDto {

    private Long id;
    private WebhookEventType eventType;
    private String domainCodename;
    private String url;
    private String requestBody;
    private Integer responseStatus;
    private String responseBody;
    private LocalDateTime executionTimestamp;
}
