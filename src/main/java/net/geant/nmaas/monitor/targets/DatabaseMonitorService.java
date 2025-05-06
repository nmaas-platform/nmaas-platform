package net.geant.nmaas.monitor.targets;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.monitor.MonitorService;
import net.geant.nmaas.monitor.MonitorStatus;
import net.geant.nmaas.monitor.ServiceType;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class DatabaseMonitorService extends MonitorService {

    private final DataSource dataSource;

    @Override
    public void checkStatus() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(100)) {
                this.updateMonitorEntry(MonitorStatus.SUCCESS);
            } else {
                this.updateMonitorEntry(MonitorStatus.FAILURE);
            }
        } catch (SQLException e) {
            this.updateMonitorEntry(MonitorStatus.FAILURE);
        }
    }

    @Override
    public ServiceType getServiceType() {
        return ServiceType.DATABASE;
    }

}
