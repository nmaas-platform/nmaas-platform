package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;

import java.util.List;

public interface ApplicationBaseService {

    ApplicationBase create(ApplicationBase applicationBase);
    ApplicationBase update(ApplicationBase applicationBase);

    ApplicationBase updateOwner(ApplicationBase applicationBase);

    void updateApplicationVersionState(String name, String version, ApplicationState state);

    List<ApplicationBase> findAll();
    List<ApplicationBase> findAllActiveApps();

    ApplicationBase getBaseApp(Long id);
    ApplicationBase findByName(String name);

    boolean exists(String name);
    boolean isAppActive(ApplicationBase application);

    void deleteAppBase(ApplicationBase base);
}
