package net.geant.nmaas.portal.api.domain;

public class AppRate {
	Double rate;

	public AppRate() {
	}
	
	public AppRate(Integer rate) {
		this.rate = (rate != null ? rate.doubleValue() : null);
	}
	
	public AppRate(Double rate) {
		this.rate = rate;
	}
	
	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}
}
