package net.geant.nmaas.portal.persistent.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Comment;

@Repository
public interface CommentRepository extends PagingAndSortingRepository<Comment, Long> {
	Long countByApplication(Application app);
	Page<Comment> findByApplication(Application app, Pageable pagable);
}
