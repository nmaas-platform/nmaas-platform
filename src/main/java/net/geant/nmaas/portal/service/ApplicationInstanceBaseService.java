package net.geant.nmaas.portal.service;

import net.geant.nmaas.api.dto.applications.AppInstanceBase;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationInstanceBaseService {

    Page<AppInstanceBase> findAll(Pageable pageable);
    Page<AppInstanceBase> findAll(Pageable pageable,boolean deployed,String search);

    Page<AppInstanceBase> findAllByOwner(User owner, Pageable pageable);
    Page<AppInstanceBase> findAllByOwner(User owner, Pageable pageable, boolean deployed,String search);
    Page<AppInstanceBase> findAllByOwner(User owner, Domain domain, Pageable pageable);
    Page<AppInstanceBase> findAllByOwner(User owner, Domain domain, Pageable pageable, boolean deployed);
    Page<AppInstanceBase> findAllByOwner(User owner, Domain domain, Pageable pageable, boolean deployed,String search);

    Page<AppInstanceBase> findAllByOwner(User owner, Pageable pageable, String search);
    Page<AppInstanceBase> findAllByDomain(Domain domain, Pageable pageable,String search);
    Page<AppInstanceBase> findAllByDomain(Domain domain, Pageable pageable, boolean deployed);
    Page<AppInstanceBase> findAllByDomain(Domain domain, Pageable pageable, boolean deployed,String search);
}
