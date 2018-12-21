package net.geant.nmaas.externalservices.inventory.shibboleth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ShibbolethMonitorService implements MonitorService {

    private ShibbolethConfigManager shibbolethManager;

    private MonitorManager monitorManager;

    @Autowired
    public void setShibbolethManager(ShibbolethConfigManager shibbolethManager) {
        this.shibbolethManager = shibbolethManager;
    }

    @Autowired
    public void setMonitorManager(MonitorManager monitorManager) {
        this.monitorManager = monitorManager;
    }

    @Override
    public void checkStatus() {
        String url = shibbolethManager.getLoginUrl().replaceFirst("^https", "http");
        try{
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(shibbolethManager.getTimeout() * 1000);
            connection.setReadTimeout(shibbolethManager.getTimeout() * 1000);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            if(responseCode >= 200 && responseCode <= 399){
                monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.SUCCESS);
                log.debug("Shibboleth instance is running");
            } else{
                monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.FAILURE);
                log.error("Shibboleth instance is not running");
            }
        } catch (IOException | IllegalStateException e){
            monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.FAILURE);
            log.error("Shibboleth instance is not running -> " + e.getMessage());
        }
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.SHIBBOLETH;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.checkStatus();
    }
}
