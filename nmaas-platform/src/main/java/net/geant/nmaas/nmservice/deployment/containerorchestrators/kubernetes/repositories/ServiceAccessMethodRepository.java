package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.repositories;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.ServiceAccessMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceAccessMethodRepository extends JpaRepository<ServiceAccessMethod, Long> {

}
