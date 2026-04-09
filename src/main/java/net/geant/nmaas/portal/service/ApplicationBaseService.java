package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.applications.ApplicationBaseViewS;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationState;

import java.util.List;

public interface ApplicationBaseService {

    ApplicationBase create(ApplicationBase applicationBase);

    ApplicationBase update(ApplicationBase applicationBase);

    ApplicationBase updateOwner(Long id, String owner);

    void updateApplicationVersionState(String name, String version, ApplicationState state);

    List<ApplicationBase> findAll();

    List<ApplicationBase> findAllActiveApps();

    List<ApplicationBaseViewS> findAllActiveAppsSmall();

    ApplicationBase getBaseApp(Long id);

    ApplicationBase getByIdForUpdate(Long id);

    ApplicationBase findByName(String name);

    ApplicationBase findByVersionId(Long versionId);

    boolean exists(String name);

    boolean isAppActive(ApplicationBase application);

    void deleteAppBase(ApplicationBase base);
}
