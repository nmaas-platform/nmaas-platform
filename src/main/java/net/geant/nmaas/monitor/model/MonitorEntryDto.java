package net.geant.nmaas.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.TimeFormat;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonitorEntryDto {

    private Long id;
    private ServiceType serviceName;
    private MonitorStatus status;
    private Date lastCheck;
    private Date lastSuccess;
    private Long checkInterval;
    private TimeFormat timeFormat;
    private boolean active;

    public MonitorEntryDto(ServiceType serviceName, Long checkInterval, TimeFormat timeFormat) {
        this.serviceName = serviceName;
        this.checkInterval = checkInterval;
        this.timeFormat = timeFormat;
        this.active = true;
    }
}
