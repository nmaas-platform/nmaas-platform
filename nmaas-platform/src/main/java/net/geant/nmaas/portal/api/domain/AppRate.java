package net.geant.nmaas.portal.api.domain;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppRate {
	Integer rate;

	Double averageRate;

	Map<Integer, Long> rating;
	
	public AppRate(Integer rate) {
		this.rate = rate;
	}

	public AppRate(Double averageRate, Map<Integer,Long> rating){
		this.averageRate = averageRate;
		this.rating = rating;
	}

}
