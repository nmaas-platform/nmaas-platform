package net.geant.nmaas.externalservices.inventory.shibboleth;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import net.geant.nmaas.portal.api.security.SSOConfigManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
@Log4j2
public class ShibbolethMonitorService extends MonitorService {

    private final SSOConfigManager ssoConfigManager;

    @Override
    public void checkStatus() {
        if(!ssoConfigManager.isSsoLoginAllowed()) {
            log.debug("SSO login option is disabled. Skipping Shibboleth instance status check");
            return;
        }
        if(!ssoConfigManager.isConfigValid()) {
            log.debug("Invalid SSO configuration. Skipping Shibboleth instance status check");
            return;
        }

        String url = ssoConfigManager.getLoginUrl();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(ssoConfigManager.getTimeout() * 1000);
            connection.setReadTimeout(ssoConfigManager.getTimeout() * 1000);
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
        } catch (IOException | IllegalStateException e) {
            this.updateMonitorEntry(MonitorStatus.FAILURE);
            log.warn("Shibboleth instance is not running -> " + e.getMessage());
        }
    }

    @Override
    public boolean schedulable() {
        // schedule Shibboleth monitoring only if SSO login option is enabled
        return ssoConfigManager.isSsoLoginAllowed();
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.SHIBBOLETH;
    }

}
