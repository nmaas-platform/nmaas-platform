package net.geant.nmaas.monitor.entities;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.TimeFormat;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonitorEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Enumerated
    private ServiceType serviceName;

    @Enumerated
    private MonitorStatus status = MonitorStatus.NOT_CHECKED;

    private Date lastCheck;

    private Date lastSuccess;

    @Column(nullable = false)
    private Long checkInterval;

    @Enumerated
    private TimeFormat timeFormat;

    @Column(nullable = false)
    private boolean active;
}
