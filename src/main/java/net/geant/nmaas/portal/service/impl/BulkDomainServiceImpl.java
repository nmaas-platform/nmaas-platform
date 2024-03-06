package net.geant.nmaas.portal.service.impl;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.dcn.deployment.DcnDeploymentType;
import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import net.geant.nmaas.externalservices.kubernetes.KubernetesClusterIngressManager;
import net.geant.nmaas.portal.api.bulk.BulkDeploymentViewS;
import net.geant.nmaas.portal.api.bulk.BulkType;
import net.geant.nmaas.portal.api.bulk.CsvDomain;
import net.geant.nmaas.portal.api.domain.KeyValueView;
import net.geant.nmaas.portal.api.domain.DomainDcnDetailsView;
import net.geant.nmaas.portal.api.domain.DomainGroupView;
import net.geant.nmaas.portal.api.domain.DomainRequest;
import net.geant.nmaas.portal.api.domain.DomainTechDetailsView;
import net.geant.nmaas.portal.api.domain.UserViewMinimal;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.BulkDeployment;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentEntry;
import net.geant.nmaas.portal.persistent.entity.BulkDeploymentState;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.entity.UserRole;
import net.geant.nmaas.portal.persistent.repositories.BulkDeploymentRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRoleRepository;
import net.geant.nmaas.portal.service.BulkDomainService;
import net.geant.nmaas.portal.service.DomainGroupService;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_DOMAIN_CODENAME;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_DOMAIN_ID;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_DOMAIN_NAME;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_USER_EMAIL;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_USER_ID;
import static net.geant.nmaas.portal.api.bulk.BulkDeploymentEntryView.BULK_ENTRY_DETAIL_KEY_USER_NAME;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_DOMAIN_ADMIN;
import static net.geant.nmaas.portal.persistent.entity.Role.ROLE_VL_DOMAIN_ADMIN;

@Service
@Slf4j
public class BulkDomainServiceImpl implements BulkDomainService {

    private final DomainService domainService;
    private final DomainGroupService domainGroupService;
    private final UserService userService;
    private final KubernetesClusterIngressManager kubernetesClusterIngressManager;

    private final BulkDeploymentRepository bulkDeploymentRepository;
    private final ModelMapper modelMapper;

    private final UserRoleRepository userRoleRepository;

    private final int domainCodenameMaxLength;

    @Value("${nmaas.domains.namespace.custom.annotations:false}")
    private Boolean includeDomainAnnotations;

    public BulkDomainServiceImpl(
            DomainService domainService,
            DomainGroupService domainGroupService,
            UserService userService,
            BulkDeploymentRepository bulkDeploymentRepository,
            KubernetesClusterIngressManager kubernetesClusterIngressManager,
            ModelMapper modelMapper,
            UserRoleRepository userRoleRepository,
            @Value("${nmaas.portal.domains.codename.length}") int domainCodenameMaxLength) {
        this.domainService = domainService;
        this.domainGroupService = domainGroupService;
        this.userService = userService;
        this.kubernetesClusterIngressManager = kubernetesClusterIngressManager;
        this.bulkDeploymentRepository = bulkDeploymentRepository;
        this.modelMapper = modelMapper;
        this.domainCodenameMaxLength = domainCodenameMaxLength;
        this.userRoleRepository = userRoleRepository;
    }

    public BulkDeploymentViewS handleBulkCreation(List<CsvDomain> domainSpecs, UserViewMinimal creator) {
        log.info("Handling bulk domain creation with {} entries", domainSpecs.size());
        BulkDeployment bulkDeployment = createBulkDeployment(creator);

        List<BulkDeploymentEntry> bulkDeploymentEntries = new ArrayList<>();

        domainSpecs.forEach(domainSpec -> {
            Domain domain = createDomainIfNotExists(bulkDeploymentEntries, domainSpec);
            // domain groups creation and domain assignment
            createMissingGroupsAndAssignDomain(domainSpec, domain, creator);
            // if user exist update role in domain to domain admin
            createUserAccountIfNotExists(bulkDeploymentEntries, domainSpec, domain);
        });

        bulkDeployment.setEntries(bulkDeploymentEntries);
        if (bulkDeploymentEntries.stream().allMatch(entry -> entry.getState().equals(BulkDeploymentState.COMPLETED))) {
            bulkDeployment.setState(BulkDeploymentState.COMPLETED);
        } else if (bulkDeploymentEntries.stream().anyMatch(entry -> entry.getState().equals(BulkDeploymentState.FAILED))) {
            bulkDeployment.setState(BulkDeploymentState.FAILED);
        }
        return modelMapper.map(bulkDeploymentRepository.save(bulkDeployment), BulkDeploymentViewS.class);
    }

    private Domain createDomainIfNotExists(List<BulkDeploymentEntry> result, CsvDomain csvDomain) {
        log.info("Processing csvDomain {}", csvDomain.getDomainName());
        Domain domain = null;
        Optional<Domain> domainFromDb = domainService.findDomain(csvDomain.getDomainName());
        if (domainFromDb.isPresent()) {
            domain = domainFromDb.get();
            result.add(BulkDeploymentEntry.builder()
                    .type(BulkType.DOMAIN)
                    .state(BulkDeploymentState.COMPLETED)
                    .created(false)
                    .details(prepareBulkDomainDeploymentDetailsMap(domain)).build()
            );
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
            DomainDcnDetailsView domainDcnDetails = new DomainDcnDetailsView(null, domainCodename, true, DcnDeploymentType.MANUAL, null);

            List<KeyValueView> annotations = new ArrayList<>();
            if(includeDomainAnnotations != null && includeDomainAnnotations) {
                // TODO move to different place
                this.domainService.getAnnotations().forEach(annotation -> {
                    annotations.add(annotation);
                });
                log.info("Add global {} annotations to domain request {}", this.domainService.getAnnotations().size() ,csvDomain.getDomainName());
            
            }

            domain = domainService.createDomain(
                    new DomainRequest(csvDomain.getDomainName(), domainCodename, domainDcnDetails, domainTechDetails, true, annotations));
            domainService.storeDcnInfo(prepareDcnInfo(domain));
            result.add(BulkDeploymentEntry.builder()
                    .type(BulkType.DOMAIN)
                    .state(BulkDeploymentState.COMPLETED)
                    .created(true)
                    .details(prepareBulkDomainDeploymentDetailsMap(domain)).build()
            );
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

    private DcnInfo prepareDcnInfo(Domain domain) {
        DcnInfo dcnInfo = new DcnInfo();
        dcnInfo.setDomain(domain.getCodename());
        dcnInfo.setName(domain.getCodename() + "-" + System.nanoTime());
        dcnInfo.setDcnDeploymentType(domain.getDomainDcnDetails().getDcnDeploymentType());
        dcnInfo.setState(DcnDeploymentState.VERIFIED);
        return dcnInfo;
    }

    private void createMissingGroupsAndAssignDomain(CsvDomain csvDomain, Domain domain, UserViewMinimal creator) {
        List<String> groupNames = Arrays.stream(csvDomain.getDomainGroups().replaceAll("\\s", "").split(",")).collect(Collectors.toList());
        groupNames.removeAll(Arrays.asList("", null));
        groupNames.forEach(groupName -> {
            log.info("Adding domain {} to group {}", domain.getName(), groupName);
            if (!domainGroupService.existDomainGroup(groupName, groupName)) {
                domainGroupService.createDomainGroup(new DomainGroupView(null, groupName, groupName, null, null, List.of(creator)));
                domainGroupService.addDomainsToGroup(List.of(domain), groupName);
                User user = userService.findByUsername(creator.getUsername()).orElseThrow(() -> new MissingElementException("User not found"));
                userRoleRepository.save(new UserRole(user, domain, ROLE_VL_DOMAIN_ADMIN));
            } else {
                domainGroupService.addDomainsToGroup(List.of(domain), groupName);
            }
        });
    }

    private void createUserAccountIfNotExists(List<BulkDeploymentEntry> result, CsvDomain csvDomain, Domain domain) {
        if (userService.existsByUsername(csvDomain.getAdminUserName()) || userService.existsByEmail(csvDomain.getEmail())) {
            log.info("User {} with email {} already exists in database", csvDomain.getAdminUserName(), csvDomain.getEmail());
            User user = userService.findByUsername(csvDomain.getAdminUserName()).orElseGet(() -> userService.findByEmail(csvDomain.getEmail()));
            if (!userService.hasPrivilege(user, domain, ROLE_DOMAIN_ADMIN)) {
                user.setNewRoles(ImmutableSet.of(new UserRole(user, domain, ROLE_DOMAIN_ADMIN)));
                userService.update(user);
            }
            result.add(BulkDeploymentEntry.builder().type(BulkType.USER).state(BulkDeploymentState.COMPLETED).created(false).details(prepareBulkUserCreationDetailsMap(user)).build());
        } else {
            User user = userService.registerBulk(csvDomain, domainService.getGlobalDomain().orElseThrow(), domain);
            result.add(BulkDeploymentEntry.builder().type(BulkType.USER).state(BulkDeploymentState.COMPLETED).created(true).details(prepareBulkUserCreationDetailsMap(user)).build());
        }
    }

    private static BulkDeployment createBulkDeployment(UserViewMinimal creator) {
        BulkDeployment bulkDeployment = new BulkDeployment();
        bulkDeployment.setType(BulkType.DOMAIN);
        bulkDeployment.setState(BulkDeploymentState.PENDING);
        bulkDeployment.setCreatorId(creator.getId());
        bulkDeployment.setCreationDate(OffsetDateTime.now());
        return bulkDeployment;
    }

    private static Map<String, String> prepareBulkDomainDeploymentDetailsMap(Domain domain) {
        Map<String, String> details = new HashMap<>();
        details.put(BULK_ENTRY_DETAIL_KEY_DOMAIN_ID, domain.getId().toString());
        details.put(BULK_ENTRY_DETAIL_KEY_DOMAIN_NAME, domain.getName());
        details.put(BULK_ENTRY_DETAIL_KEY_DOMAIN_CODENAME, domain.getCodename());
        return details;
    }

    private static Map<String, String> prepareBulkUserCreationDetailsMap(User user) {
        Map<String, String> details = new HashMap<>();
        details.put(BULK_ENTRY_DETAIL_KEY_USER_ID, user.getId().toString());
        details.put(BULK_ENTRY_DETAIL_KEY_USER_NAME, user.getUsername());
        details.put(BULK_ENTRY_DETAIL_KEY_USER_EMAIL, user.getEmail());
        return details;
    }

}
