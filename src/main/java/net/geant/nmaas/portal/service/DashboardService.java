package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.dashboard.DashboardDto;
import net.geant.nmaas.portal.api.dashboard.DomainDashboardDto;

import java.time.OffsetDateTime;

public interface DashboardService {

    DashboardDto getSystemDashboard(OffsetDateTime startDate, OffsetDateTime endDate);

    DomainDashboardDto getDomainDashboard(Long domainId);

    DashboardDto getOperatorDashboard();

}
