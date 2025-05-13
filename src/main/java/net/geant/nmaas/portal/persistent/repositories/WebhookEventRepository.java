package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.WebhookEvent;
import net.geant.nmaas.portal.persistent.entity.WebhookEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.stream.Stream;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    @Query("select w.id from WebhookEvent w where w.eventType = :eventType")
    Stream<Long> findIdByEventType(WebhookEventType eventType);
}
