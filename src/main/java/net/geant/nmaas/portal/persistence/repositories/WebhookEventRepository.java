package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.WebhookEvent;
import net.geant.nmaas.portal.persistence.entity.WebhookEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    @Query("select w.id from WebhookEvent w where w.eventType = :eventType")
    List<Long> findIdByEventType(WebhookEventType eventType);

    @Query("select w.id from WebhookEvent w where w.eventType = :eventType and w.domain is null union select w.id from WebhookEvent w where w.eventType = :eventType and w.domain.codename = :domainCodename")
    List<Long> findIdByEventTypeAndDomain(@Param("eventType") WebhookEventType eventType, @Param("domainCodename") String domainCodename);

    List<WebhookEvent> findByDomain_Id(Long domainId);

    Optional<WebhookEvent> findByIdAndDomain_Id(Long id, Long domainId);

    Page<WebhookEvent> findByNameContaining(String name, Pageable pageable);

    Page<WebhookEvent> findAllByDomainId(Long domainId, Pageable pageable);

    Page<WebhookEvent> findAllByDomainIdAndNameContaining(Long domainId, String searchValue, Pageable pageable);

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
