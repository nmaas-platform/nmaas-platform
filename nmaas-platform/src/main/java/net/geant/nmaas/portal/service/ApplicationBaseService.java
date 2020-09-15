package net.geant.nmaas.portal.service;

import java.util.List;

import net.geant.nmaas.portal.api.domain.ApplicationBaseView;
import net.geant.nmaas.portal.api.domain.ApplicationMassiveView;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

public interface ApplicationBaseService {

    ApplicationBase create(ApplicationBase applicationBase);
    ApplicationBase update(ApplicationBase applicationBase);

    void updateApplicationVersionState(String name, String version, ApplicationState state);

    List<ApplicationBase> findAll();
    List<ApplicationBase> findAllActiveOrDisabledApps();

    ApplicationBase getBaseApp(Long id);
    ApplicationBase findByName(String name);

    boolean isAppActive(ApplicationBase application);

}
