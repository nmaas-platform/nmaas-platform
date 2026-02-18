package net.geant.nmaas.portal.domain.converters;

import net.geant.nmaas.api.dto.applications.ApplicationSubscriptionBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationSubscription;
import org.modelmapper.AbstractConverter;

public class ApplicationSubscriptionConverter
        extends AbstractConverter<ApplicationSubscription, ApplicationSubscriptionBase> {

    @Override
    protected ApplicationSubscriptionBase convert(ApplicationSubscription source) {
        if (source == null) {
            return null;
        }
        Long domainId = null;
        if (source.getDomain() != null) {
            domainId = source.getDomain().getId();
        }
        Long applicationId = null;
        if (source.getApplication() != null) {
            applicationId = source.getApplication().getId();
        }
        return new ApplicationSubscriptionBase(domainId, applicationId);
    }

}
