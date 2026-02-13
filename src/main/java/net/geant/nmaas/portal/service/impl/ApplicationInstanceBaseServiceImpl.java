package net.geant.nmaas.portal.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.portal.domain.AppInstanceBase;
import net.geant.nmaas.portal.domain.AppInstanceState;
import net.geant.nmaas.portal.persistence.entity.AppInstance;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceBaseService;
import net.geant.nmaas.portal.service.ApplicationInstanceService;
import org.apache.commons.lang3.Validate;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApplicationInstanceBaseServiceImpl implements ApplicationInstanceBaseService {

    private final AppInstanceRepository appInstanceRepo;
    protected final ModelMapper modelMapper;
    protected final ApplicationBaseService appBaseService;
    private final AppDeploymentMonitor appDeploymentMonitor;
    private final ApplicationInstanceService instanceService;


    @Override
    public Page<AppInstanceBase> findAll(Pageable pageable) {
        return getAppInstanceBases(appInstanceRepo.findAllNotDeleted(pageable), pageable);
    }

    @Override
    public Page<AppInstanceBase> findAll(Pageable pageable, boolean deployed, String search) {
        Sort mapped = Sort.by(
                pageable.getSort().stream().map(order -> {
                    String p = order.getProperty();
                    String mappedProp = switch (p){
                        case "owner" -> "owner.username";
                        case "state" -> "l.state";
                        case "application" -> "application.name";
                        default -> p;
                    };
                    return new Sort.Order(order.getDirection(), mappedProp);
                }).toList()
        );
        Pageable newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mapped);
        return getAppInstanceBases(appInstanceRepo.findAllNotDeletedByDeploy(
                        search,
                        newPageable,
                        deployed),
                newPageable);
    }

    @Override
    public Page<AppInstanceBase> findAllByOwner(User owner, Pageable pageable) {
        checkParam(owner);
        Page<AppInstance> page = appInstanceRepo.findAllByOwner(owner, pageable);
        List<AppInstanceBase> filtered = page.getContent()
                .stream()
                .filter(appInstance -> !appInstance.getDomain().isDeleted())
                .map(this::mapAppInstanceBase)
                .toList();
        return new PageImpl<>(filtered, pageable, page.getTotalElements());
    }

    @Override
    public Page<AppInstanceBase> findAllByOwner(User owner, Pageable pageable, boolean deployed, String search) {
        checkParam(owner);
        return getAppInstanceBases(appInstanceRepo.findAllNotDeletedByOwnerAndByDeployAndSearch(
                        owner,
                        search,
                        deployed,
                        pageable
                ), pageable
        );
    }

    @Override
    public Page<AppInstanceBase> findAllByOwner(User owner, Pageable pageable, String search) {
        checkParam(owner);
        return getAppInstanceBases(appInstanceRepo.findAllNotDeletedByOwnerAndSearch(
                        owner,
                        search,
                        pageable
                ), pageable
        );
    }

    @Override
    public Page<AppInstanceBase> findAllByOwner(User owner, Domain domain, Pageable pageable) {
        checkParam(owner);
        checkParam(domain);
        return getAppInstanceBases(appInstanceRepo.findAllNotDeletedByOwnerAndSearch(
                        owner,
                        null,
                        pageable
                ), pageable
        );
    }

    @Override
    public Page<AppInstanceBase> findAllByOwner(User owner, Domain domain, Pageable pageable, boolean deployed) {
        checkParam(owner);
        checkParam(domain);
        return getAppInstanceBases(appInstanceRepo.findAllNotDeletedByOwnerAndDomainAndByDeployAndSearch(
                        owner,
                        null,
                        domain,
                        deployed,
                        pageable
                ), pageable
        );
    }

    @Override
    public Page<AppInstanceBase> findAllByOwner(User owner, Domain domain, Pageable pageable, boolean deployed, String search) {
        checkParam(owner);
        checkParam(domain);
        return getAppInstanceBases(appInstanceRepo.findAllNotDeletedByOwnerAndDomainAndByDeployAndSearch(
                        owner,
                        search,
                        domain,
                        deployed,
                        pageable
                ), pageable
        );
    }

    @Override
    public Page<AppInstanceBase> findAllByDomain(Domain domain, Pageable pageable, String search) {
        checkParam(domain);
        return getAppInstanceBases(appInstanceRepo.findAllNotDeletedByDomainAndSearch(
                domain,
                null,
                pageable), pageable
        );
    }

    @Override
    public Page<AppInstanceBase> findAllByDomain(Domain domain, Pageable pageable, boolean deployed) {
        checkParam(domain);
        return getAppInstanceBases(appInstanceRepo.findAllNotDeletedByDomainAndByDeployAndSearch(
                domain,
                null,
                deployed,
                pageable), pageable
        );
    }

    @Override
    public Page<AppInstanceBase> findAllByDomain(Domain domain, Pageable pageable, boolean deployed, String search) {
        checkParam(domain);
        Sort mapped = Sort.by(
                pageable.getSort().stream().map(order -> {
                    String p = order.getProperty();
                    String mappedProp = switch (p){
                        case "owner" -> "owner.username";
                        case "state" -> "l.state";
                        case "application" -> "application.name";
                        default -> p;
                    };
                return new Sort.Order(order.getDirection(), mappedProp);
                }).toList()
        );
        Pageable newPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), mapped);
        return getAppInstanceBases(appInstanceRepo.findAllNotDeletedByDomainAndByDeployAndSearch(
                domain,
                search,
                deployed,
                newPageable), newPageable
        );
    }

    private AppInstanceBase mapAppInstanceBase(AppInstance appInstance) {
        if (appInstance == null) {
            return null;
        }
        AppInstanceBase ai = modelMapper.map(appInstance, AppInstanceBase.class);
        ai.setApplicationBaseId(appBaseService.findByVersionId(appInstance.getApplication().getId()).getId());
        return addAppInstanceBaseProperties(ai, appInstance);
    }

    private AppInstanceBase addAppInstanceBaseProperties(AppInstanceBase ai, AppInstance appInstance) {
        try {
            ai.setState(mapAppInstanceState(this.appDeploymentMonitor.state(appInstance.getInternalId())));
            ai.setUserFriendlyState(ai.getState().getUserFriendlyState());
        } catch (Exception e) {
            ai.setState(AppInstanceState.UNKNOWN);
            ai.setUserFriendlyState(ai.getState().getUserFriendlyState());
        }

        if (!ai.getDomainId().equals(appInstance.getDomain().getId())) {
            ai.setDomainId(appInstance.getDomain().getId());
        }

        // add information about app instance upgrade possibility
        if (!List.of(AppInstanceState.DONE, AppInstanceState.REMOVED).contains(ai.getState())) {
            ai.setUpgradePossible(instanceService.checkUpgradePossible(appInstance.getId()));
        }

        return ai;
    }

    public static AppInstanceState mapAppInstanceState(AppLifecycleState state) {
        AppInstanceState appInstanceState;
        switch (state) {
            case REQUESTED:
                appInstanceState = AppInstanceState.REQUESTED;
                break;
            case REQUEST_VALIDATION_IN_PROGRESS:
            case REQUEST_VALIDATED:
                appInstanceState = AppInstanceState.VALIDATION;
                break;
            case DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.PREPARATION;
                break;
            case DEPLOYMENT_ENVIRONMENT_PREPARED:
            case MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.CONNECTING;
                break;
            case MANAGEMENT_VPN_CONFIGURED:
                appInstanceState = AppInstanceState.CONFIGURATION_AWAITING;
                break;
            case APPLICATION_CONFIGURATION_IN_PROGRESS:
            case APPLICATION_CONFIGURED:
            case APPLICATION_DEPLOYMENT_IN_PROGRESS:
            case APPLICATION_DEPLOYED:
            case APPLICATION_CONFIGURATION_UPDATE_IN_PROGRESS:
            case APPLICATION_CONFIGURATION_UPDATED:
            case APPLICATION_RESTART_IN_PROGRESS:
            case APPLICATION_RESTARTED:
            case APPLICATION_PAUSE_IN_PROGRESS:
            case APPLICATION_UPGRADE_IN_PROGRESS:
            case APPLICATION_UPGRADED:
            case APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS:
                appInstanceState = AppInstanceState.DEPLOYING;
                break;
            case APPLICATION_DEPLOYMENT_VERIFIED:
                appInstanceState = AppInstanceState.RUNNING;
                break;
            case APPLICATION_PAUSED:
                appInstanceState = AppInstanceState.PAUSED;
                break;
            case APPLICATION_REMOVAL_IN_PROGRESS:
                appInstanceState = AppInstanceState.UNDEPLOYING;
                break;
            case APPLICATION_REMOVED:
            case APPLICATION_CONFIGURATION_REMOVAL_IN_PROGRESS:
            case APPLICATION_CONFIGURATION_REMOVED:
                appInstanceState = AppInstanceState.DONE;
                break;
            case INTERNAL_ERROR:
            case REQUEST_VALIDATION_FAILED:
            case DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED:
            case MANAGEMENT_VPN_CONFIGURATION_FAILED:
            case APPLICATION_CONFIGURATION_FAILED:
            case APPLICATION_DEPLOYMENT_VERIFICATION_FAILED:
            case APPLICATION_REMOVAL_FAILED:
            case APPLICATION_RESTART_FAILED:
            case APPLICATION_PAUSE_FAILED:
            case APPLICATION_CONFIGURATION_UPDATE_FAILED:
            case APPLICATION_DEPLOYMENT_FAILED:
            case APPLICATION_CONFIGURATION_REMOVAL_FAILED:
            case APPLICATION_UPGRADE_FAILED:
                appInstanceState = AppInstanceState.FAILURE;
                break;
            case FAILED_APPLICATION_REMOVED:
                appInstanceState = AppInstanceState.REMOVED;
                break;
            case UNKNOWN:
            default:
                appInstanceState = AppInstanceState.UNKNOWN;
                break;
        }
        return appInstanceState;
    }

    private void checkParam(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }
        checkParam(user.getId());
    }

    private void checkParam(Long id) {
        Validate.isTrue(id != null, "Id is null");
    }

    private void checkParam(Domain domain) {
        if (domain == null) {
            throw new IllegalArgumentException("domain is null");
        }
        checkParam(domain.getId());
    }

    private Page<AppInstanceBase> getAppInstanceBases(Page<AppInstance> page, Pageable pageable) {
        List<AppInstanceBase> filtered = page.getContent()
                .stream()
                .map(this::mapAppInstanceBase)
                .toList();
        return new PageImpl<>(filtered, pageable, page.getTotalElements());
    }

}
