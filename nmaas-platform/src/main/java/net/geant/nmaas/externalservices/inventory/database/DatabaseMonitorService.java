package net.geant.nmaas.externalservices.inventory.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import javax.sql.DataSource;
import net.geant.nmaas.monitor.MonitorManager;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseMonitorService implements MonitorService {

    private final DataSource dataSource;

    private MonitorManager monitorManager;

    @Autowired
    public DatabaseMonitorService(DataSource dataSource, MonitorManager monitorManager){
        this.dataSource = dataSource;
        this.monitorManager = monitorManager;
    }

    @Override
    public void checkStatus() {
        try {
            Connection connection = dataSource.getConnection();
            if (connection.isValid(100)) {
                monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.SUCCESS);
            } else {
                monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.FAILURE);
            }
        } catch(SQLException e){
            monitorManager.updateMonitorEntry(new Date(), this.getServiceType(), MonitorStatus.FAILURE);
        }
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.DATABASE;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.checkStatus();
    }
}
