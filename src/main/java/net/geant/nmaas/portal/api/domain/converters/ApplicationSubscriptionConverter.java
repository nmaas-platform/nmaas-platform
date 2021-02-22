package net.geant.nmaas.portal.api.domain.converters;

import org.modelmapper.AbstractConverter;

import net.geant.nmaas.portal.api.domain.ApplicationSubscriptionBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;

public class ApplicationSubscriptionConverter
		extends AbstractConverter<ApplicationSubscription, ApplicationSubscriptionBase> {

	@Override
	protected ApplicationSubscriptionBase convert(ApplicationSubscription source) {
		if(source == null) return null;

		Long domainId = null;
		if(source.getDomain() != null) domainId = source.getDomain().getId();
		Long applicationId = null;
		if(source.getApplication() != null) applicationId = source.getApplication().getId();

		return new ApplicationSubscriptionBase(domainId, applicationId);
	}

}
