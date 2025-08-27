package net.geant.nmaas.portal.service;

import net.geant.nmaas.portal.api.domain.AppInstanceBase;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationInstanceBaseService {

    Page<AppInstanceBase> findAll(Pageable pageable);
    Page<AppInstanceBase> findAll(Pageable pageable,boolean deployed);

    Page<AppInstanceBase> findAllByOwner(User owner, Pageable pageable);
    Page<AppInstanceBase> findAllByOwner(User owner, Pageable pageable, boolean deployed);
    Page<AppInstanceBase> findAllByOwner(User owner, Domain domain, Pageable pageable);
    Page<AppInstanceBase> findAllByOwner(User owner, Domain domain, Pageable pageable, boolean deployed);

    Page<AppInstanceBase> findAllByDomain(Domain domain, Pageable pageable);
    Page<AppInstanceBase> findAllByDomain(Domain domain, Pageable pageable, boolean deployed);
}
