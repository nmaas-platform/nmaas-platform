package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.Variable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariableRepository extends JpaRepository<Variable, Long> {

    List<Variable> findByDomainIsNull();

    List<Variable> findByDomainId(Long domainId);

    Optional<Variable> findByNameAndDomainIsNull(String name);

    Optional<Variable> findByNameAndDomainId(String name, Long domainId);

    boolean existsByNameAndDomainIsNull(String name);

    boolean existsByNameAndDomainId(String name, Long domainId);

}
