package net.geant.nmaas.portal.persistent.repositories;

import org.springframework.data.repository.PagingAndSortingRepository;

import net.geant.nmaas.portal.persistent.entity.AppInstance;

public interface AppInstanceRepository extends PagingAndSortingRepository<AppInstance, Long> {

}
