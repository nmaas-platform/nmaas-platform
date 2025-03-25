package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.info.DashboardDeploymentsView;
import net.geant.nmaas.portal.api.info.DashboardView;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistent.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistent.repositories.DomainRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DashboardService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final DomainService domainService;
    private final DomainRepository domainRepository;
    private final ApplicationInstanceService applicationInstanceService;
    private final AppInstanceRepository appInstanceRepo;
    private final ApplicationBaseRepository applicationBaseRepository;


    @Override
    public DashboardView getSystemDomainDashboard() {

        long weekTimestamp = System.currentTimeMillis() - Duration.ofDays(7).toMillis();

        List<String> baseNames = applicationBaseRepository.findAllNames();
        Map<String, Integer> applicationDeploymentCountPerName = new HashMap<>();

        List<DashboardDeploymentsView> deploymentsViews = appInstanceRepo.findAllInTimePeriod(weekTimestamp)
                .stream().map(entry -> {
                    return DashboardDeploymentsView.builder().user(entry.getOwner().getUsername())
                            .domainName(entry.getDomain().getName())
                            .applicationName(entry.getApplication().getName())
                            .applicationVersion(entry.getApplication().getVersion()).build();
                }).toList();

        baseNames.forEach(name -> {
            applicationDeploymentCountPerName.put(name, appInstanceRepo.countByName(name));
        });


        return DashboardView.builder()
                .domainsCount(domainRepository.count())
                .userCount(userRepository.count())
                .instanceCount(appInstanceRepo.count())
                .instanceCountInPeriod(appInstanceRepo.countAllDeployedSinceTime(weekTimestamp))
                .instanceCountInPeriodDetails(deploymentsViews)
                .popularApps(applicationDeploymentCountPerName).build();
    }

}
