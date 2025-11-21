package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.WebhookHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookHistoryRepository extends JpaRepository<WebhookHistory, Long>, JpaSpecificationExecutor<WebhookHistory> {
}
