package net.geant.nmaas.dcn.deployment.repositories;

import net.geant.nmaas.dcn.deployment.entities.DcnDeploymentState;
import net.geant.nmaas.dcn.deployment.entities.DcnInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface DcnInfoRepository extends JpaRepository<DcnInfo, Long> {

    Optional<DcnInfo> findByDomain(String domain);

    @Query("SELECT d.state FROM DcnInfo d WHERE d.domain = :domain")
    Optional<DcnDeploymentState> getStateByDomain(@Param("domain") String domain);

}
