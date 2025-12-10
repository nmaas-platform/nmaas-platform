package net.geant.nmaas.portal.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "webhooks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String targetUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebhookEventType eventType;

    @Column
    private String tokenValue;

    @Column
    private String authorizationHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", referencedColumnName = "id")
    private Domain domain;

    @Lob
    @Column
    private String template;

    public WebhookEvent(Long id, String name, String targetUrl, WebhookEventType eventType) {
        this.id = id;
        this.name = name;
        this.targetUrl = targetUrl;
        this.eventType = eventType;
    }
}
