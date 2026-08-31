package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.api.dto.domains.DomainGroupBaseDto;
import net.geant.nmaas.portal.persistence.entity.DomainGroup;
import net.geant.nmaas.portal.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomainGroupRepository extends JpaRepository<DomainGroup, Long> {

    boolean existsByName(String name);

    boolean existsByCodename(String codename);

    Optional<DomainGroup> findByCodename(String codeName);

    @Query("SELECT DISTINCT dg.id FROM DomainGroup dg JOIN dg.domains d WHERE d.codename = :codename")
    List<String> findDomainGroupIdsByDomainCodename(@Param("codename") String codename);

    @Query("""
            SELECT dg
            FROM DomainGroup dg
            WHERE (:search IS NULL
            OR LOWER(dg.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(dg.codename) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            GROUP BY dg.id, dg.name, dg.codename
            ORDER BY LOWER(dg.name)
            """)
    List<DomainGroup> findAllWithSearch(@Param("search") String search);

    @Query("""
            SELECT dg
            FROM DomainGroup dg
            JOIN dg.managers m
            WHERE m = :manager
            AND (:search IS NULL
            OR LOWER(dg.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(dg.codename) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            GROUP BY dg.id, dg.name, dg.codename
            ORDER BY LOWER(dg.name)
            """)
    List<DomainGroup> findAllByManagersWithSearch(User manager, @Param("search") String search);

    @Query("""
            SELECT new net.geant.nmaas.api.dto.domains.DomainGroupBaseDto(
                dg.id,
                dg.name,
                dg.codename,
                CAST(COUNT(d) as integer) as noOfDomains)
            FROM DomainGroup dg
            LEFT JOIN dg.domains d
            WHERE (:search IS NULL
            OR LOWER(dg.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(dg.codename) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            GROUP BY dg.id, dg.name, dg.codename
            """)
    Page<DomainGroupBaseDto> getAllBaseDtoWithSearch(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT new net.geant.nmaas.api.dto.domains.DomainGroupBaseDto(
                dg.id,
                dg.name,
                dg.codename,
                CAST(COUNT(d) as integer) as noOfDomains)
            FROM DomainGroup dg
            JOIN dg.managers m
            LEFT JOIN dg.domains d
            WHERE m = :manager
            AND (:search IS NULL
            OR LOWER(dg.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            OR LOWER(dg.codename) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            GROUP BY dg.id, dg.name, dg.codename
            """)
    Page<DomainGroupBaseDto> getAllBaseDtoByManagerWithSearch(User manager, @Param("search") String search, Pageable pageable);

}
