package net.geant.nmaas.portal.persistent.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import net.geant.nmaas.portal.persistent.entity.AppInstance;
import net.geant.nmaas.portal.persistent.entity.User;

public interface AppInstanceRepository extends PagingAndSortingRepository<AppInstance, Long> {
	Page<AppInstance> findAllByOwner(User owner, Pageable pageable);
}
