package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.info.DashboardView;
import net.geant.nmaas.portal.api.info.DomainDashboardView;

import java.time.OffsetDateTime;

public interface DashboardService {

    DashboardView getSystemDashboard(OffsetDateTime startDate, OffsetDateTime endDate);

    DomainDashboardView getDomainDashboard(Long domainId);

}
