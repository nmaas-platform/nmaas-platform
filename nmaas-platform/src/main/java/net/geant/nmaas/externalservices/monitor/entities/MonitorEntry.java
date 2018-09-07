package net.geant.nmaas.externalservices.monitor.entities;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.externalservices.monitor.MonitorStatus;
import net.geant.nmaas.externalservices.monitor.ServiceType;
import net.geant.nmaas.externalservices.monitor.TimeFormat;

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
    private MonitorStatus status;

    private Date lastCheck;

    private Date lastSuccess;

    @Column(nullable = false)
    private Long checkInterval;

    @Enumerated
    private TimeFormat timeFormat;
}
