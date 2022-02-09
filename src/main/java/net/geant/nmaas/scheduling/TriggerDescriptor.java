package net.geant.nmaas.scheduling;

import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.TimeFormat;
import org.quartz.Trigger;

import java.time.ZoneId;
import java.util.TimeZone;

import static org.quartz.CronExpression.isValidExpression;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class TriggerDescriptor {

    private final ServiceType serviceName;
    private final Long checkInterval;
    private final TimeFormat timeFormat;
    private final String cron;

    public TriggerDescriptor(ServiceType serviceName, Long checkInterval, TimeFormat timeFormat) {
        this.serviceName = serviceName;
        this.checkInterval = checkInterval;
        this.timeFormat = timeFormat;
        this.cron = createCronString();
    }

    private String createCronString() {
        if (timeFormat.equals(TimeFormat.H) && checkInterval.equals(24L)) {
            return "0 0 0 * * ?";
        } else if(timeFormat.equals(TimeFormat.H)) {
            return String.format("0 0 0/%d * * ?", checkInterval);
        }
        return String.format("0 0/%d * * * ?", checkInterval);
    }

    Trigger buildTrigger() {
        if(cron != null && !cron.isEmpty()) {
            if(!isValidExpression(cron)) {
                throw new IllegalStateException("Provided interval is incorrect. You can choose from 1-59 minutes and 1-24 hours.");
            }
            return newTrigger()
                    .withIdentity(serviceName.getName())
                    .withSchedule(cronSchedule(cron)
                                    .withMisfireHandlingInstructionFireAndProceed()
                                    .inTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault())))
                    .usingJobData("cron", cron)
                    .build();
        }
        throw new IllegalStateException(String.format("Building trigger for %s failed", serviceName.getName()));
    }
}
