package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.repositories;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceStorageVolume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceStorageVolumeRepository extends JpaRepository<ServiceStorageVolume, Long> {

}
