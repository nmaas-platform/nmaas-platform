package net.geant.nmaas.portal.api.market;

import java.security.Principal;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import net.geant.nmaas.portal.api.domain.ApiResponse;
import net.geant.nmaas.portal.api.domain.AppRate;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.RatingRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;

@RestController
@RequestMapping("/api/apps/{appId}/rate")
public class RatingController extends AppBaseController {
	
	RatingRepository ratingRepo;
	
	UserRepository userRepo;

	@Autowired
	public RatingController(RatingRepository ratingRepo, UserRepository userRepo){
		this.ratingRepo = ratingRepo;
		this.userRepo = userRepo;
	}

	@GetMapping
	public AppRate getAppRating(@PathVariable("appId") Long appId) throws MissingElementException {
		Application app = getApp(appId);
		Double rate = ratingRepo.getApplicationRating(app.getId());
		return (rate != null ? new AppRate(rate) : new AppRate());
	}

	@GetMapping(value="/my")
	@PreAuthorize("hasPermission(#appId, 'application', 'READ')")
	public AppRate getMyAppRating(@PathVariable("appId") Long appId, @NotNull Principal principal) throws MissingElementException {
		User user = getUser(principal.getName());
		return getUserAppRating(appId, user.getId());
	}

	@GetMapping(value="/user/{userId}")
	@PreAuthorize("hasPermission(#appId, 'application', 'READ')")
	public AppRate getUserAppRating(@PathVariable("appId") Long appId, @PathVariable("userId") Long userId) throws MissingElementException {
		Application app = getApp(appId);
		User user = getUser(userId);
		
		net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId appRateId = new net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId(app.getId(), user.getId());
		Optional<net.geant.nmaas.portal.persistent.entity.AppRate> appRate = ratingRepo.findById(appRateId);
		return appRate.isPresent() ? new AppRate(appRate.get().getRate()) : new AppRate();
	}


	@PostMapping(value="/my/{rate}")
	@PreAuthorize("hasPermission(#appId, 'application', 'WRITE')")
	@Transactional
	public ApiResponse setUserAppRating(@PathVariable("appId") Long appId, @PathVariable("rate") Integer rate, @NotNull Principal principal) throws MissingElementException {
		Application app = getApp(appId);
		User user = getUser(principal.getName());
		
		net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId appRatingId = new net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId(app.getId(), user.getId());
		net.geant.nmaas.portal.persistent.entity.AppRate appRate = ratingRepo.findById(appRatingId)
				.orElse(new net.geant.nmaas.portal.persistent.entity.AppRate(appRatingId));
		
		appRate.setRate(normalizeRate(rate));
		
		ratingRepo.save(appRate);
		
		return new ApiResponse(true);
	}
	
	
	
	protected Integer normalizeRate(Integer rate) throws MissingElementException {
		if(rate == null)
			throw new MissingElementException("Missing rate value.");
		
		if(rate > 5)
			return 5;
		else if(rate < 0)
			return 0;
		else
			return rate;
	}
}
