package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.info.DashboardView;
import net.geant.nmaas.portal.api.info.DomainDashboardView;

public interface DashboardService {

    public DashboardView getSystemDashboard();

    public DomainDashboardView getSystemDomainDashboard(Long domainId);
}
