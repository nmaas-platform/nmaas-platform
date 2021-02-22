package net.geant.nmaas.nmservice.configuration.repositories;

import java.util.List;
import net.geant.nmaas.nmservice.configuration.entities.ConfigFileTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigFileTemplatesRepository extends JpaRepository<ConfigFileTemplate, Long> {
    List<ConfigFileTemplate> getAllByApplicationId(Long applicationId);
}
