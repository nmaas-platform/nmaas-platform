package net.geant.nmaas.portal.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppRate {
	Double rate;
	
	public AppRate(Integer rate) {
		this.rate = (rate != null ? rate.doubleValue() : null);
	}

}
