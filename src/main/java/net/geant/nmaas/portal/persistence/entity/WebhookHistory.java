package net.geant.nmaas.portal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long webhookEventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookEventType eventType;

    @Column
    private String domainCodename;

    @Column(nullable = false)
    private String url;

    @Lob
    @Column(name = "request_body", columnDefinition = "CLOB")
    private String requestBody;

    @Column
    private Integer responseStatus;

    @Lob
    @Column(name = "response_body", columnDefinition = "CLOB")
    private String responseBody;

    private LocalDateTime executionTimestamp;
}
