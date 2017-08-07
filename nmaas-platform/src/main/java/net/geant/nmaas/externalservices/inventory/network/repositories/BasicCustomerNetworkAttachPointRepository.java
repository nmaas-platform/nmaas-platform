package net.geant.nmaas.externalservices.inventory.network.repositories;

import net.geant.nmaas.externalservices.inventory.network.BasicCustomerNetworkAttachPoint;
import net.geant.nmaas.externalservices.inventory.network.DockerHostAttachPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Repository
public interface BasicCustomerNetworkAttachPointRepository extends JpaRepository<BasicCustomerNetworkAttachPoint, Long> {

    Optional<BasicCustomerNetworkAttachPoint> findByCustomerId(long customerId);

}
