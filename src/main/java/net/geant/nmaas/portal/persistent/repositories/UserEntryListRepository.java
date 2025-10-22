package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.api.domain.UserListEntry;
import net.geant.nmaas.portal.persistent.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntryListRepository extends JpaRepository<User, Long> {

    @Query("""
            SELECT new net.geant.nmaas.portal.api.domain.UserListEntry(
                        user,
                        (SELECT MAX(l.date) FROM UserLoginRegister l WHERE l.userId = user.id ) as lastSuccessfulLoginDate,
                        (SELECT MIN(l.date) FROM UserLoginRegister l WHERE l.userId = user.id ) as firstLoginDate
                        )
            FROM User user
            WHERE (:search IS NULL OR LOWER(user.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            """)
    Page<UserListEntry> findAll(@Param("search") String searchValue, Pageable pageable);

    @Query("""
            SELECT new net.geant.nmaas.portal.api.domain.UserListEntry(
                        user,
                        (SELECT MAX(l.date) FROM UserLoginRegister l WHERE l.userId = user.id ) as lastSuccessfulLoginDate,
                        (SELECT MIN(l.date) FROM UserLoginRegister l WHERE l.userId = user.id ) as firstLoginDate
                        )
            FROM User user JOIN UserRole userRole ON userRole.id.user.id = user.id
            WHERE (:search IS NULL OR LOWER(user.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            AND (userRole.id.domain.id = :domainId)
            """)
    Page<UserListEntry> findAllByDomainId(@Param("domainId") long domainId,@Param("search") String searchValue, Pageable pageable);
}
