package net.geant.nmaas.portal.service;

import java.util.List;
import net.geant.nmaas.portal.api.domain.ApplicationView;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

public interface ApplicationBaseService {

    ApplicationBase createApplicationOrAddNewVersion(ApplicationView application);

    void updateApplicationBase(ApplicationView application);

    void updateApplicationBase(ApplicationBase application);

    void updateApplicationVersion(String name, String version, ApplicationState state);

    List<ApplicationBase> findAll();

    List<ApplicationBase> findAllActiveOrDisabledApps();

    ApplicationBase getBaseApp(Long id);

    ApplicationBase findByName(String name);

    boolean isAppActive(ApplicationBase application);

}
