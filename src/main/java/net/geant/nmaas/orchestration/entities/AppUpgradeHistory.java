package net.geant.nmaas.orchestration.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.geant.nmaas.orchestration.AppUpgradeMode;
import net.geant.nmaas.orchestration.AppUpgradeStatus;
import net.geant.nmaas.orchestration.Identifier;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "app_upgrade_history")
public class AppUpgradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** Unique identifier of this deployment. */
    @ToString.Include
    @Column(nullable = false, unique = true)
    private Identifier deploymentId;

    @Column(nullable = false)
    private Date timestamp;

    /** Identifier of the previous application version. */
    @Column(nullable = false)
    private Identifier previousApplicationId;

    /** Identifier of the target application version. */
    @Column(nullable = false)
    private Identifier targetApplicationId;

    @Enumerated(EnumType.STRING)
    private AppUpgradeMode mode;

    @Enumerated(EnumType.STRING)
    private AppUpgradeStatus status;

}
