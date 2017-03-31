package net.geant.nmaas.portal.api.market;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.AppDeploymentMonitor;
import net.geant.nmaas.orchestration.AppLifecycleManager;
import net.geant.nmaas.orchestration.AppLifecycleState;
import net.geant.nmaas.orchestration.Identifier;
import net.geant.nmaas.orchestration.exceptions.InvalidDeploymentIdException;
import net.geant.nmaas.portal.api.domain.AppInstance;
import net.geant.nmaas.portal.api.domain.AppInstanceState;
import net.geant.nmaas.portal.api.domain.AppInstanceStatus;
import net.geant.nmaas.portal.api.domain.AppInstanceSubscription;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.AppInstanceRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;

@RestController("/portal/api/apps/instances")
public class AppInstanceController extends AppBaseController {

	@Autowired
	AppLifecycleManager appLifecycleManager;
	
	@Autowired
	AppDeploymentMonitor appDeploymentMonitor;
	
	@Autowired
	AppInstanceRepository appInstanceRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	ModelMapper modelMapper;
	
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(method=RequestMethod.GET)
	public List<AppInstance> getAllInstances(Pageable pageable) {
		return appInstanceRepo.findAll(pageable).getContent().stream().map(appInstance -> modelMapper.map(appInstance, AppInstance.class)).collect(Collectors.toList());
	}
	
	@RequestMapping(value="/my", method=RequestMethod.GET)
	public List<AppInstance> getMyAllInstances(@NotNull Principal principal, Pageable pageable) throws MissingElementException {		
		return getUserAppInstances(principal.getName(), pageable);
	}
	
	@RequestMapping(value="/user/{username}", method=RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<AppInstance> getUserAllInstances(@PathVariable("username") String username, Pageable pageable) throws MissingElementException {
		return getUserAppInstances(username, pageable);
	}	
	
	private List<AppInstance> getUserAppInstances(String username, Pageable pageable) throws MissingElementException {
		Optional<User> user = userRepo.findByUsername(username);
		if(!user.isPresent())
			throw new MissingElementException("User not found");
		
		return appInstanceRepo.findAllByOwner(user.get(), pageable).getContent().stream().map(appInstance -> modelMapper.map(appInstance, AppInstance.class)).collect(Collectors.toList());
	}
	
	
	@RequestMapping(value="/{appInstanceId}", method=RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#appInstanceId, 'appInstance', 'owner')")
	public AppInstance getAppInstance(@PathVariable(value="appInstanceId") Long appInstanceId, @NotNull Principal principal) throws MissingElementException {
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = appInstanceRepo.findOne(appInstanceId);
		if(appInstance == null)
			throw new MissingElementException("App instance not found.");
		
		return modelMapper.map(appInstance, AppInstance.class);
	}
	
	@RequestMapping(method=RequestMethod.POST)
	@Transactional
	public Id createAppInstance(@RequestBody(required=true) AppInstanceSubscription appInstanceSubscription, @NotNull Principal principal) throws MissingElementException {
		Application app = getApp(appInstanceSubscription.getApplicationId());
		User user = getUser(principal.getName());
		
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = new net.geant.nmaas.portal.persistent.entity.AppInstance(app, appInstanceSubscription.getName());
		
		Identifier internalId = appLifecycleManager.deployApplication(new Identifier(Long.toString(user.getId())), new Identifier(Long.toString(app.getId())));
		appInstance.setInternalId(internalId);
		
		appInstanceRepo.save(appInstance);
		
		return new Id(appInstance.getId());
	}
	
	@RequestMapping(value="/{appInstanceId}", method=RequestMethod.DELETE)
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#appInstanceId, 'appInstance', 'owner')")	
	@Transactional
	public void deleteAppInstance(@PathVariable(value="appInstanceId") Long appInstanceId, @NotNull Principal principal) throws MissingElementException, ProcessingException {
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);
		
		try {
			appLifecycleManager.removeApplication(appInstance.getInternalId());
		} catch (InvalidDeploymentIdException e) {
			throw new ProcessingException("Missing app instance");
		}
	}
	
	@RequestMapping(value="/{appInstanceId}/state", method=RequestMethod.POST)
	@PreAuthorize("hasPermission(#appInstanceId, 'appInstance', 'owner')")		
	@Transactional
	public void applyConfiguration(@PathVariable(value="appInstanceId") Long appInstanceId, @RequestBody String configuration, @NotNull Principal principal) throws MissingElementException, ProcessingException {
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);
		
		AppInstanceStatus status = getState(appInstanceId, principal);
		if(status.getState() != AppInstanceState.CONFIGURATION_AWAITING)
			throw new ProcessingException("App instance configuration cannot be applied in state " + status.getState());
		
		boolean valid = validJSON(configuration);
		if(!valid)
			throw new ProcessingException("Configuration is not in valid JSON format");
		
		appInstance.setConfiguration(configuration);
		appInstanceRepo.save(appInstance);
		
		try {
			appLifecycleManager.applyConfiguration(appInstance.getInternalId(), new AppConfiguration(
																						new Identifier(Long.toString(appInstance.getApplication().getId())), 
																						configuration));
		} catch (InvalidDeploymentIdException e) {
			throw new ProcessingException("Missing app instance");
		}
	}
	
	@RequestMapping(value="/{appInstanceId}/state", method=RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasPermission(#appInstanceId, 'appInstance', 'owner')")		
	public AppInstanceStatus getState(@PathVariable(value="appInstanceId", required=true) Long appInstanceId, @NotNull Principal principal) throws MissingElementException, ProcessingException {
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = getAppInstance(appInstanceId);
		
		AppLifecycleState state = AppLifecycleState.UNKNOWN;
		try {
			state = appDeploymentMonitor.state(appInstance.getInternalId());
		} catch (InvalidDeploymentIdException e) {
			throw new ProcessingException("Missing app instance");			
		}
		
		return prepareAppInstanceStatus(appInstanceId, state);
	}
	
	private boolean validJSON(String json) {
		try {
			new JSONObject(json);
			return true;
		} catch(JSONException ex) {
			return false;
		}
	}
	
	private AppInstanceStatus prepareAppInstanceStatus(Long appInstanceId, AppLifecycleState state) {
		AppInstanceStatus appInstanceStatus = new AppInstanceStatus();
		appInstanceStatus.setAppInstanceId(appInstanceId);
		appInstanceStatus.setDetails(state.name());
		
		AppInstanceState appInstanceState;
		
		switch(state) {
		case REQUESTED:	appInstanceState = AppInstanceState.SUBSCRIBED;
						break;
		case REQUEST_VALIDATION_IN_PROGRESS:
		case REQUEST_VALIDATED:
						appInstanceState = AppInstanceState.VALIDATION;
						break;
		
		case DEPLOYMENT_ENVIRONMENT_PREPARATION_IN_PROGRESS:
		case DEPLOYMENT_ENVIRONMENT_PREPARED:
						appInstanceState = AppInstanceState.PREPARATION;
						break;
						
		case MANAGEMENT_VPN_CONFIGURATION_IN_PROGRESS:
						appInstanceState = AppInstanceState.CONNECTING;
						break;
		case MANAGEMENT_VPN_CONFIGURED:						
						appInstanceState = AppInstanceState.CONFIGURATION_AWAITING;
						break;
		case APPLICATION_CONFIGURATION_IN_PROGRESS:
		case APPLICATION_CONFIGURED:
		case APPLICATION_DEPLOYMENT_IN_PROGRESS:		
		case APPLICATION_DEPLOYMENT_FAILED:
		case APPLICATION_DEPLOYMENT_VERIFICATION_IN_PROGRESS:
		case APPLICATION_DEPLOYMENT_VERIFIED:
			appInstanceState = AppInstanceState.DEPLOYING;
			break;
		case APPLICATION_DEPLOYED:
			appInstanceState = AppInstanceState.RUNNING;
			break;			
		case APPLICATION_REMOVAL_IN_PROGRESS:
			appInstanceState = AppInstanceState.UNDEPLOYING;
			break;						
		case APPLICATION_REMOVED:
			appInstanceState = AppInstanceState.DONE;
			break;
			
		case UNKNOWN:
		case INTERNAL_ERROR:
		case GENERIC_ERROR:
		case REQUEST_VALIDATION_FAILED:
		case DEPLOYMENT_ENVIRONMENT_PREPARATION_FAILED:
		case MANAGEMENT_VPN_CONFIGURATION_FAILED:
		case APPLICATION_CONFIGURATION_FAILED:
		case APPLICATION_DEPLOYMENT_VERIFICATION_FAILED:
		case APPLICATION_REMOVAL_FAILED:
		default:
			appInstanceState = AppInstanceState.FAILURE;
			break;
		}
		
		appInstanceStatus.setState(appInstanceState);
		
		return appInstanceStatus;
	}
	
	private net.geant.nmaas.portal.persistent.entity.AppInstance getAppInstance(Long appInstanceId) throws MissingElementException {
		if(appInstanceId == null)
			throw new MissingElementException("Missing app instance id.");
		
		net.geant.nmaas.portal.persistent.entity.AppInstance appInstance = appInstanceRepo.findOne(appInstanceId);
		if(appInstance == null)
			throw new MissingElementException("App instance not found.");
		
		return appInstance;
	}
	
}
