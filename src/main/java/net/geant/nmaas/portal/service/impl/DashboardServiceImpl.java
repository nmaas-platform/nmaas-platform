package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.api.dto.domains.DomainBaseDto;
import net.geant.nmaas.api.dto.domains.DomainGroupDto;
import net.geant.nmaas.api.dto.dashboard.ApplicationDeployedDto;
import net.geant.nmaas.api.dto.dashboard.DashboardDeploymentsDto;
import net.geant.nmaas.api.dto.dashboard.DashboardDto;
import net.geant.nmaas.api.dto.dashboard.DomainAppInstanceDto;
import net.geant.nmaas.api.dto.dashboard.DomainDashboardDto;
import net.geant.nmaas.api.dto.dashboard.DomainGroupDashboardDto;
import net.geant.nmaas.api.dto.dashboard.UserLoginsDto;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.entity.UserLoginRegister;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistence.repositories.ApplicationBaseRepository;
import net.geant.nmaas.portal.persistence.repositories.DomainRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import net.geant.nmaas.portal.service.DashboardService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserLoginRegisterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final DomainGroupService domainGroupService;

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

        baseNames.forEach(name -> applicationDeploymentCountPerName.put(name, appInstanceRepository.countByName(name)));

        // filter not deployed application
        applicationDeploymentCountPerName.entrySet().removeIf(app -> app.getValue() == 0);

        return DashboardDto.builder()
                .domainsCount(domainRepository.count())
                .userCount(userRepository.findAll().stream().filter(User::isEnabled).count())
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
        List<DomainAppInstanceDto> upgradePossible = new ArrayList<>();

        if (domain.isPresent()) {
            Domain dom = domain.get();

            List<User> domainUsers = domainService.getMembers(domainId);
            List<AppInstance> apps = appInstanceRepository.findAllActiveInDomain(dom.getCodename());

            domainUsers.forEach(user -> {
                Optional<UserLoginRegister> register = userLoginRegisterService.getLastLogin(user);
                register.ifPresent(userLoginRegister -> userLogins.put(getUserPreferredUsername(user), userLoginRegister.getDate()));
                appsDeployed.put(getUserPreferredUsername(user), appInstanceRepository.countAllByOwnerAndDomain(user, dom));
            });
            apps.forEach(app -> upgradePossible.add(DomainAppInstanceDto.builder()
                    .appId(app.getId())
                    .baseAppId(appBaseService.findByName(app.getApplication().getName()).getId())
                    .appName(app.getApplication().getName())
                    .instanceName(app.getName())
                    .appVersion(app.getApplication().getVersion())
                    .upgradePossible(applicationInstanceService.checkUpgradePossible(app.getId())).build()));

            Map<String, Integer> sortedAppsDeployed = appsDeployed.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
            List<ApplicationDeployedDto> sortedAppsDeployedDto = sortedAppsDeployed.entrySet().stream()
                    .map(entry -> ApplicationDeployedDto.builder()
                            .userName(entry.getKey())
                            .count(entry.getValue())
                            .build())
                    .toList();
            List<UserLoginsDto> userLoginsDto = userLogins.entrySet().stream()
                    .map(entry -> UserLoginsDto.builder()
                            .userName(entry.getKey())
                            .lastLogin(entry.getValue())
                            .build())
                    .toList();
            return DomainDashboardDto.builder()
                    .userLogins(userLoginsDto)
                    .applicationDeployed(sortedAppsDeployedDto)
                    .applicationUpgradeStatus(upgradePossible)
                    .build();
        } else {
            log.error("Domain {} not present. Returning empty...", domainId);
            return DomainDashboardDto.builder().build();
        }
    }
    @Override
    public DomainGroupDashboardDto getDomainGroupDashboard(Long groupId) {
        DomainGroupDashboardDto result = new DomainGroupDashboardDto();
        List<DomainGroupDashboardDto.DomainDto> domains = new ArrayList<>();
        Map<String, OffsetDateTime> userLogins = new HashMap<>();
        DomainGroupDto domainGroup = domainGroupService.getDomainGroup(groupId);

        Set<User> groupUsers = new HashSet<>();


        for(DomainBaseDto domain: domainGroup.getDomains()){
            DomainDashboardDto domainDashboardDto = getDomainDashboard(domain.getId());
            groupUsers.addAll(domainService.getMembers(domain.getId()));
            domains.add(
                    new DomainGroupDashboardDto.DomainDto(
                    domain.getName(),
                    domainDashboardDto.getApplicationDeployed(),
                    domainDashboardDto.getApplicationUpgradeStatus())
            );
        }
        groupUsers.forEach(user -> {
            Optional<UserLoginRegister> register = userLoginRegisterService.getLastLogin(user);
            register.ifPresent(userLoginRegister -> userLogins.put(getUserPreferredUsername(user), userLoginRegister.getDate()));
        });
        List<UserLoginsDto> userLoginsDto = userLogins.entrySet().stream()
                .map(entry -> UserLoginsDto.builder()
                        .userName(entry.getKey())
                        .lastLogin(entry.getValue())
                        .build())
                .toList();
        result.setDomains(domains);
        result.setUserLogins(userLoginsDto);
        return result;
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
