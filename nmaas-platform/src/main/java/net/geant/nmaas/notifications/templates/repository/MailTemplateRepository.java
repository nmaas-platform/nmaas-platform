package net.geant.nmaas.notifications.templates.repository;

import java.util.Optional;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.notifications.templates.entities.MailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailTemplateRepository extends JpaRepository<MailTemplate, Long> {
    Optional<MailTemplate> findByMailType(MailType mailType);
    boolean existsByMailType(MailType mailType);
}
