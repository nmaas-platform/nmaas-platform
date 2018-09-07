package net.geant.nmaas.externalservices.monitor.scheduling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.geant.nmaas.externalservices.monitor.ServiceType;
import net.geant.nmaas.externalservices.monitor.TimeFormat;
import org.quartz.Trigger;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobDescriptor {
    private ServiceType serviceName;

    private Long checkInterval;

    private TimeFormat timeFormat;

    Trigger buildTrigger(){
        return new TriggerDescriptor(serviceName, checkInterval, timeFormat).buildTrigger();
    }
}
