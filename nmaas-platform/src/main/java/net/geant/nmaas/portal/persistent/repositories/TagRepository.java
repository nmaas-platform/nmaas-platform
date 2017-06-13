package net.geant.nmaas.portal.persistent.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import net.geant.nmaas.portal.persistent.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
	Tag findByName(String source);
}
