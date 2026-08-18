package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.DashboardDto;
import net.geant.nmaas.api.dto.DomainDashboardDto;
import net.geant.nmaas.api.dto.DomainGroupDashboardDto;

import java.time.OffsetDateTime;

public interface DashboardService {

    DashboardDto getSystemDashboard(OffsetDateTime startDate, OffsetDateTime endDate);

    DomainDashboardDto getDomainDashboard(Long domainId);
    DomainGroupDashboardDto getDomainGroupDashboard(Long domainId);

    DashboardDto getOperatorDashboard();

}
