package net.geant.nmaas.externalservices.inventory.shibboleth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class ShibbolethMonitorService extends MonitorService {

    private ShibbolethConfigManager shibbolethManager;

    @Autowired
    public void setShibbolethManager(ShibbolethConfigManager shibbolethManager) {
        this.shibbolethManager = shibbolethManager;
    }

    @Override
    public void checkStatus() {
        String url = shibbolethManager.getLoginUrl();
        try{
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(shibbolethManager.getTimeout() * 1000);
            connection.setReadTimeout(shibbolethManager.getTimeout() * 1000);
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            int responseCode = connection.getResponseCode();
            if(responseCode >= 200 && responseCode <= 399){
                this.updateMonitorEntry(MonitorStatus.SUCCESS);
                log.trace("Shibboleth instance is running");
            } else{
                this.updateMonitorEntry(MonitorStatus.FAILURE);
                log.warn("Shibboleth instance is not running");
            }
        } catch (IOException | IllegalStateException e){
            this.updateMonitorEntry(MonitorStatus.FAILURE);
            log.warn("Shibboleth instance is not running -> " + e.getMessage());
        }
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.SHIBBOLETH;
    }

}
