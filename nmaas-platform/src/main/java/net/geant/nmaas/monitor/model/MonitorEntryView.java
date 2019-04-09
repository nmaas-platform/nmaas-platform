package net.geant.nmaas.monitor.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.TimeFormat;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MonitorEntryView {
    private Long id;
    private ServiceType serviceName;
    private MonitorStatus status;
    private Date lastCheck;
    private Date lastSuccess;
    private Long checkInterval;
    private TimeFormat timeFormat;
    private boolean active;

    public MonitorEntryView(ServiceType serviceName, Long checkInterval, TimeFormat timeFormat){
        this.serviceName = serviceName;
        this.checkInterval = checkInterval;
        this.timeFormat = timeFormat;
        this.active = true;
    }
}
