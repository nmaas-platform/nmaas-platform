package net.geant.nmaas.orchestration.entities;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.OffsetDateTime;

/**
 * NMAAS-967 a record storing information about user terms acceptance
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppTermsAcceptance {

    /**
     * artificial identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Application name
     */
    private String appName;

    /**
     * Application Instance ID (terms were accepted while deploying this instance)
     */
    private Long appInstanceId;

    /**
     * Username of the user who accepted terms
     */
    private String username;

    /**
     * Acceptance Statement - a word typed by user as acceptance
     */
    private String termsAcceptanceStatement;

    /**
     * Content of terms to be accepted, e.g. license
     */
    private String termsContent;

    /**
     * Timestamp of acceptance, generated at server side
     */
    private OffsetDateTime acceptanceDate;
}
