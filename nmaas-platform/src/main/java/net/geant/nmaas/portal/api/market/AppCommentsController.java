package net.geant.nmaas.portal.api.market;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import net.geant.nmaas.portal.api.domain.Comment;
import net.geant.nmaas.portal.api.domain.CommentRequest;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.CommentRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;

@RestController
@RequestMapping("/api/apps/{appId}/comments")
public class AppCommentsController extends AppBaseController {

	CommentRepository commentRepo;
	
	UserRepository userRepo;

	@Autowired
	public AppCommentsController(CommentRepository commentRepo, UserRepository userRepo){
		this.commentRepo = commentRepo;
		this.userRepo = userRepo;
	}
			
	@GetMapping
	@PreAuthorize("hasPermission(null, 'comment', 'READ')")
	public List<Comment> getComments(@PathVariable(value="appId", required=true) Long appId, Pageable pageable) {
		Application app = getApp(appId);
		Page<net.geant.nmaas.portal.persistent.entity.Comment> page = commentRepo.findByApplication(app, pageable);
		return page.getContent().stream().map(comment -> { 
												Comment c = modelMapper.map(comment, Comment.class); 
												if(comment.getParent() != null)
													c.setParentId(comment.getParent().getId());
												if(comment.isDeleted()) 
													c.setComment("<em>@@@\'COMMENTS.REMOVED\'</em>");
												for(Comment sub : c.getSubComments()) {
													if(sub.isDeleted())
														sub.setComment("<em>@@@\'COMMENTS.REMOVED\'</em>");
												}
													
												return c;}
											).collect(Collectors.toList());
	}
	
	
	@PostMapping
	@PreAuthorize("hasPermission(null, 'comment', 'CREATE')")
	@Transactional
	public Id addComment(@PathVariable(value="appId", required=true) Long appId, @RequestBody(required=true) CommentRequest comment, Principal principal) {
		Application app = getApp(appId);

		if(comment.getComment() == null || comment.getComment().isEmpty())
			throw new IllegalArgumentException("Comment cannot be empty");
		
		Long parentId = comment.getParentId();
		
		//Workaround problem of mapping parentId -> id
		//This should be fixed in modelmapper configuration
		comment.setParentId(null);
		net.geant.nmaas.portal.persistent.entity.Comment persistentComment = modelMapper.map(comment, net.geant.nmaas.portal.persistent.entity.Comment.class);
		if(persistentComment.getId() != null)
			throw new IllegalStateException("New comment cannot have id.");
		
		User user = userRepo.findByUsername(principal.getName()).orElseThrow(() ->
				new MissingElementException("User not found."));

		persistentComment.setApplication(app);
		persistentComment.setOwner(user);

		net.geant.nmaas.portal.persistent.entity.Comment persistentParentComment = null;
		
		if(parentId != null) {
			persistentParentComment = getComment(parentId);
			if(persistentParentComment == null)
				throw new MissingElementException("Unable to add comment to non-existing one");
			if( persistentParentComment.getApplication().getId() != appId )
				throw new ProcessingException("Unable to add comment to different application");
			persistentComment.setParent(persistentParentComment);
		}
		commentRepo.save(persistentComment);


		
		
		return new Id(persistentComment.getId());
	}

	@PostMapping(value="/{commentId}")
	@PreAuthorize("hasPermission(null, 'comment', 'WRITE')")
	@Transactional
	public void editComment(@PathVariable(value="appId", required=true) Long appId, @PathVariable(value="commentId", required=true) Long commentId, @RequestBody(required=true) CommentRequest comment, Principal principal) {
		throw new ProcessingException("Comment editing not supported.");
	}

	@DeleteMapping(value="/{commentId}")
	@PreAuthorize("hasPermission(#commentId, 'comment', 'DELETE')")
	@Transactional
	public void deleteComment(@PathVariable(value="appId") Long appId, @PathVariable(value="commentId") Long commentId) {
		net.geant.nmaas.portal.persistent.entity.Comment comment = getComment(commentId);
		comment.setDeleted(true);
		commentRepo.save(comment);
	}
	
	private net.geant.nmaas.portal.persistent.entity.Comment getComment(Long commentId) {
		if (commentId == null)
			throw new MissingElementException("Missing comment id." );
		return commentRepo.findById(commentId).orElseThrow(() ->
				new MissingElementException("Comment id=" + commentId + " not found."));
	}
	
	
}
