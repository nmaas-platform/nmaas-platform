package net.geant.nmaas.externalservices.monitor.scheduling;

import java.time.ZoneId;
import java.util.TimeZone;
import lombok.AllArgsConstructor;
import net.geant.nmaas.externalservices.monitor.ServiceType;
import static org.quartz.CronExpression.isValidExpression;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.Trigger;
import static org.quartz.TriggerBuilder.newTrigger;

@AllArgsConstructor
public class TriggerDescriptor {
    private ServiceType serviceName;
    private Long checkInterval;
    private String cron;

    public TriggerDescriptor(ServiceType serviceName, Long checkInterval){
        this.serviceName = serviceName;
        this.checkInterval = checkInterval;
        this.cron = createCronString();
    }

    private String createCronString(){
        return String.format("0 0/%d * * * ?", checkInterval);
    }

    Trigger buildTrigger(){
        if(cron != null && !cron.isEmpty()){
            if(!isValidExpression(cron)){
                throw new IllegalStateException("Provided cron exception is not valid");
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
