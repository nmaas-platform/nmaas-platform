package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    @Query("select w.id from WebhookEvent w where w.eventType = :eventType")
    List<Long> findIdByEventType(WebhookEventType eventType);
}
