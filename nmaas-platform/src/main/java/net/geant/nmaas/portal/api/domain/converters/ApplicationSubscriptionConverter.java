package net.geant.nmaas.portal.api.domain.converters;

import org.modelmapper.AbstractConverter;

import net.geant.nmaas.portal.api.domain.ApplicationSubscriptionBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;

public class ApplicationSubscriptionConverter
		extends AbstractConverter<ApplicationSubscription, ApplicationSubscriptionBase> {

	@Override
	protected ApplicationSubscriptionBase convert(ApplicationSubscription source) {
		return (source != null
				? new ApplicationSubscriptionBase(source.getDomain() != null ? source.getDomain().getId() : null,
						source.getApplication() != null ? source.getApplication().getId() : null)
				: null);
	}

}
