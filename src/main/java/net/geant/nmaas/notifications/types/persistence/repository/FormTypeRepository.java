package net.geant.nmaas.notifications.types.persistence.repository;

import net.geant.nmaas.notifications.types.persistence.entity.FormType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormTypeRepository extends JpaRepository<FormType, String> {
}
