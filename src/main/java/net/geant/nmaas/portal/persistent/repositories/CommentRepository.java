package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.Comment;

@Repository
public interface CommentRepository extends PagingAndSortingRepository<Comment, Long> {
	Long countByApplication(ApplicationBase app);
	Page<Comment> findByApplication(ApplicationBase app, Pageable pageable);
}
