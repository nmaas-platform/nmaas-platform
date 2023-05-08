package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.bulk.CsvBean;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.domain.DomainDcnDetailsView;
import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.DomainTechDetailsView;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_DOMAIN_ADMIN;

@Service
@Slf4j
public class BulkDomainServiceImpl implements BulkDomainService {

    private final DomainService domainService;
    private final DomainGroupService domainGroupService;
    private final UserService userService;
    private final KubernetesClusterIngressManager kubernetesClusterIngressManager;

    private final int domainCodenameMaxLength;

    public BulkDomainServiceImpl(DomainService domainService,
                                 DomainGroupService domainGroupService,
                                 UserService userService,
                                 KubernetesClusterIngressManager kubernetesClusterIngressManager,
                                 @Value("${nmaas.portal.domains.codename.length}") int domainCodenameMaxLength) {
        this.domainService = domainService;
        this.domainGroupService = domainGroupService;
        this.userService = userService;
        this.kubernetesClusterIngressManager = kubernetesClusterIngressManager;
        this.domainCodenameMaxLength = domainCodenameMaxLength;
    }

    public List<BulkDeploymentEntryView> handleBulkCreation(List<CsvBean> input) {
        log.info("Handling bulk domain creation with {} entries", input.size());

        List<BulkDeploymentEntryView> result = new ArrayList<>();
        List<CsvDomain> csvDomains = input.stream().map(d -> (CsvDomain) d).collect(Collectors.toList());

        csvDomains.forEach( csvDomain -> {
            Domain domain = createDomainIfNotExists(result, csvDomain);
            // domain groups creation and domain assignment
            createMissingGroupsAndAssignDomain(csvDomain, domain);
            // if user exist update role in domain to domain admin
            createUserAccountIfNotExists(result, csvDomain, domain);
        });
        return result;
    }

    private Domain createDomainIfNotExists(List<BulkDeploymentEntryView> result, CsvDomain csvDomain) {
        log.info("Processing csvDomain {}", csvDomain.getDomainName());
        Domain domain = null;
        Optional<Domain> domainFromDb = domainService.findDomain(csvDomain.getDomainName());
        if (domainFromDb.isPresent()) {
            domain = domainFromDb.get();
            Map<String, String> details = new HashMap<>();
            details.put("domainId", domain.getId().toString());
            details.put("domainName", domain.getName());
            details.put("domainCodename", domain.getCodename());
            result.add(new BulkDeploymentEntryView(true, false, details, BulkType.DOMAIN));
        } else {
            String domainCodename = prepareCorrectAndUniqueDomainCodename(csvDomain.getDomainName());
            DomainTechDetailsView domainTechDetails = DomainTechDetailsView.builder()
                    .domainCodename(domainCodename)
                    .kubernetesNamespace(domainCodename)
                    .externalServiceDomain(domainCodename + "." + kubernetesClusterIngressManager.getExternalServiceDomain())
                    .build();
            if (kubernetesClusterIngressManager.getIngressPerDomain()) {
                domainTechDetails.setKubernetesIngressClass(domainCodename);
            } else {
                domainTechDetails.setKubernetesIngressClass(kubernetesClusterIngressManager.getSupportedIngressClass());
            }
            DomainDcnDetailsView domainDcnDetails = new DomainDcnDetailsView(null, csvDomain.getDomainName(), true, DcnDeploymentType.MANUAL, null);
            domain = domainService.createDomain(
                    DomainRequest.builder()
                            .codename(domainCodename)
                            .name(csvDomain.getDomainName())
                            .active(true)
                            .domainDcnDetails(domainDcnDetails)
                            .domainTechDetails(domainTechDetails)
                            .build());

            Map<String, String> details = new HashMap<>();
            details.put("domainId", domain.getId().toString());
            details.put("domainName", domain.getName());
            details.put("domainCodename", domain.getCodename());
            result.add(new BulkDeploymentEntryView(true, true, details, BulkType.DOMAIN));
        }
        return domain;
    }

    private String prepareCorrectAndUniqueDomainCodename(String domainName) {
        String codeName = domainName.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
        codeName = StringUtils.substring(codeName, 0, domainCodenameMaxLength);
        if (domainService.existsDomainByCodename(codeName)) {
            boolean unique = false;
            int index = 1;
            while (!unique && index < 10) {
                codeName = StringUtils.substring(codeName, 0, -1);
                codeName = codeName + index;
                unique = !domainService.existsDomainByCodename(codeName);
                index++;
            }
            if (!unique) {
                index = 10;
                while (!unique && index < 100) {
                    codeName = StringUtils.substring(codeName, 0, -2);
                    codeName = codeName + index;
                    unique = !domainService.existsDomainByCodename(codeName);
                    index++;
                }
            }
        }
        return codeName;
    }

    private void createMissingGroupsAndAssignDomain(CsvDomain csvDomain, Domain domain) {
        List<String> groupNames = Arrays.stream(csvDomain.getDomainGroups().replaceAll("\\s", "").split(",")).collect(Collectors.toList());
        groupNames.removeAll(Arrays.asList("", null));
        groupNames.forEach( groupName -> {
            log.info("Adding domain {} to group {}", domain.getName(), groupName);
            if (!domainGroupService.existDomainGroup(groupName, groupName)) {
                domainGroupService.createDomainGroup(new DomainGroupView(null, groupName, groupName, null, null));
                domainGroupService.addDomainsToGroup(List.of(domain), groupName);
            } else {
                domainGroupService.addDomainsToGroup(List.of(domain), groupName);
            }
        });
    }

    private void createUserAccountIfNotExists(List<BulkDeploymentEntryView> result, CsvDomain csvDomain, Domain domain) {
        if (userService.existsByUsername(csvDomain.getAdminUserName()) || this.userService.existsByEmail(csvDomain.getEmail())) {
            log.info("User {} with email {} already exists in database", csvDomain.getAdminUserName(), csvDomain.getEmail());
            User user = userService.findByUsername(csvDomain.getAdminUserName()).orElseGet(() -> this.userService.findByEmail(csvDomain.getEmail()));
            if (!userService.hasPrivilege(user, domain, ROLE_DOMAIN_ADMIN)) {
                user.setNewRoles(ImmutableSet.of(new UserRole(user, domain, ROLE_DOMAIN_ADMIN)));
                userService.update(user);
            }
            Map<String, String> details = new HashMap<>();
            details.put("userId", user.getId().toString());
            details.put("userName", user.getUsername());
            details.put("email", user.getEmail());
            result.add(new BulkDeploymentEntryView(true, false, details, BulkType.USER));
        } else { //if not create user
            User user = this.userService.registerBulk(csvDomain, this.domainService.getGlobalDomain().get(), domain);
            Map<String, String> details = new HashMap<>();
            details.put("userName", user.getUsername());
            details.put("userId", user.getId().toString());
            details.put("email", user.getEmail());
            result.add(new BulkDeploymentEntryView(true, true, details, BulkType.USER));
        }
    }
    
}
