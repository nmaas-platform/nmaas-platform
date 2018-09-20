package net.geant.nmaas.nmservice.configuration.repositories;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfigurationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NmServiceConfigFileTemplatesRepository extends JpaRepository<NmServiceConfigurationTemplate, Long> {

    List<NmServiceConfigurationTemplate> findAllByApplicationId(Long applicationId);

}
