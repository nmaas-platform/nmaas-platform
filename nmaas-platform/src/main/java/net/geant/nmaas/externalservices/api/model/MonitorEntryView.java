package net.geant.nmaas.externalservices.api.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.externalservices.monitor.MonitorStatus;
import net.geant.nmaas.externalservices.monitor.ServiceType;

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
}
