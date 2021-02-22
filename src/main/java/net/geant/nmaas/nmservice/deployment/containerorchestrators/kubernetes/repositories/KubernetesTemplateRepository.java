package net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.repositories;

import net.geant.nmaas.nmservice.deployment.containerorchestrators.kubernetes.entities.KubernetesTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KubernetesTemplateRepository extends JpaRepository<KubernetesTemplate, Long> {

}
