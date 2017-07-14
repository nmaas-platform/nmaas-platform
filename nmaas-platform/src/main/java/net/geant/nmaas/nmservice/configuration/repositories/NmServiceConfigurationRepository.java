package net.geant.nmaas.nmservice.configuration.repositories;

import net.geant.nmaas.nmservice.configuration.entities.NmServiceConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface NmServiceConfigurationRepository extends JpaRepository<NmServiceConfiguration, Long> {

    Optional<NmServiceConfiguration> findByConfigId(String configId);

    @Query("SELECT n.configFileName FROM NmServiceConfiguration n WHERE n.configId = :configId")
    Optional<String> getConfigFileNameByConfigId(@Param("configId") String configId);

}
