package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.dashboard.DashboardDeploymentsDto;
import net.geant.nmaas.portal.api.dashboard.DashboardDto;
import net.geant.nmaas.portal.api.dashboard.DomainDashboardDto;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DashboardService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final DomainService domainService;
    private final DomainRepository domainRepository;
    private final ApplicationInstanceService applicationInstanceService;
    private final AppInstanceRepository appInstanceRepository;
    private final ApplicationBaseRepository applicationBaseRepository;
    private final UserLoginRegisterService userLoginRegisterService;
    private final ApplicationBaseService appBaseService;

    @Override
    public DashboardDto getSystemDashboard(OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("Processing system dashboard data request for period {} - {}", startDate, endDate);

        long startTimeStamp = startDate.toEpochSecond() * 1000;
        long endTimeStamp = endDate.toEpochSecond() * 1000;

        List<String> baseNames = applicationBaseRepository.findAllNames();
        Map<String, Integer> applicationDeploymentCountPerName = new HashMap<>();

        List<DashboardDeploymentsDto> deploymentsViews = appInstanceRepository.findAllInTimePeriod(startTimeStamp, endTimeStamp).stream()
                .map(entry -> DashboardDeploymentsDto.builder().user(entry.getOwner().getUsername())
                        .domainName(entry.getDomain().getName())
                        .applicationName(entry.getApplication().getName())
                        .instanceId(entry.getId())
                        .applicationVersion(entry.getApplication().getVersion())
                        .build())
                .toList();

        baseNames.forEach(name -> {
            applicationDeploymentCountPerName.put(name, appInstanceRepository.countByName(name));
        });

        // filter not deployed application
        applicationDeploymentCountPerName.entrySet().removeIf(app -> app.getValue() == 0);

        return DashboardDto.builder()
                .domainsCount(domainRepository.count())
                .userCount(userRepository.count())
                .instanceCount(appInstanceRepository.count())
                .instanceCountInPeriod(appInstanceRepository.countAllDeployedInTimePeriod(startTimeStamp, endTimeStamp))
                .instanceCountInPeriodDetails(deploymentsViews)
                .popularApps(applicationDeploymentCountPerName).build();
    }

    @Override
    public DomainDashboardDto getDomainDashboard(Long domainId) {
        log.info("Processing dashboard data request for domain {}", domainId);

        Optional<Domain> domain = domainService.findDomain(domainId);
        Map<String, OffsetDateTime> userLogins = new HashMap<>();
        Map<String, Integer> appsDeployed = new HashMap<>();
        List<DomainDashboardDto.DomainAppInstanceDto> upgradePossible = new ArrayList<>();

        if (domain.isPresent()) {
            Domain dom = domain.get();

            List<User> domainUsers = domainService.getMembers(domainId);
            List<AppInstance> apps = appInstanceRepository.findAllActiveInDomain(dom.getCodename());

            domainUsers.forEach(user -> {
                Optional<UserLoginRegister> register = userLoginRegisterService.getLastLogin(user);
                register.ifPresent(userLoginRegister -> userLogins.put(getUserPreferredUsername(user), userLoginRegister.getDate()));
                appsDeployed.put(getUserPreferredUsername(user), appInstanceRepository.countAllByOwnerAndDomain(user, dom));
            });
            apps.forEach(app -> {
                upgradePossible.add(DomainDashboardDto.DomainAppInstanceDto.builder()
                        .appId(app.getId())
                        .baseAppId(appBaseService.findByName(app.getApplication().getName()).getId())
                        .appName(app.getApplication().getName())
                        .instanceName(app.getName())
                        .appVersion(app.getApplication().getVersion())
                        .upgradePossible(applicationInstanceService.checkUpgradePossible(app.getId())).build());
            });

            Map<String, Integer> sortedAppsDeployed = appsDeployed.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
            return DomainDashboardDto.builder()
                    .userLogins(userLogins)
                    .applicationDeployed(sortedAppsDeployed)
                    .applicationUpgradeStatus(upgradePossible)
                    .build();
        } else {
            log.error("Domain {} not present. Returning empty...", domainId);
            return DomainDashboardDto.builder().build();
        }
    }

    @Override
    public DashboardDto getOperatorDashboard() {
        Long domainCount = domainRepository.countByActiveTrueAndDeletedFalse();
        return DashboardDto.builder()
                .domainsCount(domainCount).build();
    }

    private static String getUserPreferredUsername(User user) {
        String preferredUsername;
        if (StringUtils.isEmpty(user.getUsername())) {
            throw new IllegalArgumentException("User or username is not set");
        }
        if (user.getFirstname() != null && !user.getFirstname().isEmpty()) {
            preferredUsername = user.getFirstname() + " " + user.getLastname();
        } else {
            preferredUsername = user.getUsername();
        }
        return preferredUsername;
    }

}
