package net.geant.nmaas.portal.api.market;

import java.security.Principal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.geant.nmaas.portal.api.domain.ApiResponse;
import net.geant.nmaas.portal.api.domain.AppRate;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.RatingRepository;

@RestController
@RequestMapping("/api/apps/{appId}/rate")
public class RatingController extends AppBaseController {
	
	private RatingRepository ratingRepo;

	@Autowired
	public RatingController(RatingRepository ratingRepo){
		this.ratingRepo = ratingRepo;
	}
	
	@GetMapping
	public AppRate getAppRating(@PathVariable("appId") Long appId) {
		Application app = getApp(appId);
		Integer[] rateList = ratingRepo.getApplicationRating(app.getId());
		return (rateList.length > 0 ? new AppRate(getAverageRate(rateList), getRatingMap(rateList)) : new AppRate());
	}
	
	@GetMapping(value="/my")
	@PreAuthorize("hasPermission(#appId, 'application', 'READ')")
	public AppRate getMyAppRating(@PathVariable("appId") Long appId, @NotNull Principal principal) {
		User user = getUser(principal.getName());
		return getUserAppRating(appId, user.getId());
	}

	@GetMapping(value="/user/{userId}")
	@PreAuthorize("hasPermission(#appId, 'application', 'READ')")
	public AppRate getUserAppRating(@PathVariable("appId") Long appId, @PathVariable("userId") Long userId) {
		Application app = getApp(appId);
		User user = getUser(userId);
		
		net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId appRateId = new net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId(app.getId(), user.getId());
		Optional<net.geant.nmaas.portal.persistent.entity.AppRate> appRate = ratingRepo.findById(appRateId);
		Integer[] rateList = ratingRepo.getApplicationRating(app.getId());
		return appRate.map(appRate1 -> new AppRate(appRate1.getRate(), getAverageRate(rateList), getRatingMap(rateList))).orElseGet(AppRate::new);
	}

	@PostMapping(value="/my/{rate}")
	@PreAuthorize("hasPermission(#appId, 'application', 'WRITE')")
	@Transactional
	public ApiResponse setUserAppRating(@PathVariable("appId") Long appId, @PathVariable("rate") Integer rate, @NotNull Principal principal) {
		Application app = getApp(appId);
		User user = getUser(principal.getName());
		
		net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId appRatingId = new net.geant.nmaas.portal.persistent.entity.AppRate.AppRateId(app.getId(), user.getId());
		net.geant.nmaas.portal.persistent.entity.AppRate appRate = ratingRepo.findById(appRatingId)
				.orElse(new net.geant.nmaas.portal.persistent.entity.AppRate(appRatingId));
		
		appRate.setRate(normalizeRate(rate));
		
		ratingRepo.save(appRate);
		
		return new ApiResponse(true);
	}

	private double getAverageRate(Integer[] rateList){
		return Arrays.stream(rateList)
				.mapToInt(Integer::intValue)
				.average().orElse(0.0);
	}

	private Map<Integer, Long> getRatingMap(Integer[] rateList){
		return Arrays.stream(rateList)
				.collect(Collectors.groupingBy(s -> s, Collectors.counting()));
	}
	
	private Integer normalizeRate(Integer rate) {
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
