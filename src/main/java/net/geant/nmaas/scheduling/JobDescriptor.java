package net.geant.nmaas.scheduling;

import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.TimeFormat;
import org.quartz.Trigger;

public record JobDescriptor(ServiceType serviceName, Long checkInterval, TimeFormat timeFormat) {

    Trigger buildTrigger() {
        return new TriggerDescriptor(serviceName, checkInterval, timeFormat).buildTrigger();
    }

}
