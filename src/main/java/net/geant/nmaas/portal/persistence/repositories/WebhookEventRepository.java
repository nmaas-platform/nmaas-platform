package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    @Query("select w.id from WebhookEvent w where w.eventType = :eventType")
    List<Long> findIdByEventType(WebhookEventType eventType);

    //    @Query("""
    //            select w.id
    //            from WebhookEvent w
    //            where w.eventType = :eventType
    //              and (w.domain is null or w.domain.codename = :domainCodename)
    //            """)
    @Query("select w.id from WebhookEvent w where w.eventType = :eventType and w.domain is null union select w.id from WebhookEvent w where w.eventType = :eventType and w.domain.codename = :domainCodename")
    List<Long> findIdByEventTypeAndDomain(@Param("eventType") WebhookEventType eventType, @Param("domainCodename") String domainCodename);

    @Query("""
            select w.id
            from WebhookEvent w
            where w.eventType = :eventType
              and (
                   w.domain is null
                   or w.domain.id = :domainId
              )
            """)
    List<Long> findIdByEventTypeAndDomain(WebhookEventType eventType, Long domainId);
}
