package net.geant.nmaas.portal.persistent.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import net.geant.nmaas.portal.persistent.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
	Optional<Tag> findByName(String source);
}
