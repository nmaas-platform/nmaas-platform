package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.dashboard.DashboardDto;
import net.geant.nmaas.api.dto.dashboard.DomainDashboardDto;
import net.geant.nmaas.api.dto.dashboard.DomainGroupDashboardDto;

import java.time.OffsetDateTime;

public interface DashboardService {

    DashboardDto getSystemDashboard(OffsetDateTime startDate, OffsetDateTime endDate);

    DomainDashboardDto getDomainDashboard(Long domainId);
    DomainGroupDashboardDto getDomainGroupDashboard(Long domainId);

    DashboardDto getOperatorDashboard();

}
