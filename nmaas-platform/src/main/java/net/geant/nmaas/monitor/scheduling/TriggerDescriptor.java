package net.geant.nmaas.monitor.scheduling;

import java.time.ZoneId;
import java.util.TimeZone;
import lombok.AllArgsConstructor;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.monitor.TimeFormat;
import static org.quartz.CronExpression.isValidExpression;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;

@AllArgsConstructor
public class TriggerDescriptor {
    private ServiceType serviceName;
    private Long checkInterval;
    private TimeFormat timeFormat;
    private String cron;

    public TriggerDescriptor(ServiceType serviceName, Long checkInterval, TimeFormat timeFormat){
        this.serviceName = serviceName;
        this.checkInterval = checkInterval;
        this.timeFormat = timeFormat;
        this.cron = createCronString();
    }

    private String createCronString(){
        if(timeFormat.equals(TimeFormat.H) && checkInterval.equals(24L)){
            return "0 0 0 * * ?";
        }
        else if(timeFormat.equals(TimeFormat.H)){
            return String.format("0 0 0/%d * * ?", checkInterval);
        }
        return String.format("0 0/%d * * * ?", checkInterval);
    }

    Trigger buildTrigger(){
        if(cron != null && !cron.isEmpty()){
            if(!isValidExpression(cron)){
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
