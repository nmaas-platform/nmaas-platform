package net.geant.nmaas.portal.api.market;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/portal/api/apps/{appId}/comments")
public class AppCommentsController extends AppBaseController {

	@Autowired
	CommentRepository commentRepo;
	
	@Autowired
	UserRepository userRepo;
			
	@RequestMapping(method=RequestMethod.GET)
	public List<Comment> getComments(@PathVariable(value="appId", required=true) Long appId, Pageable pageable) throws MissingElementException {
		Application app = getApp(appId);
		Page<net.geant.nmaas.portal.persistent.entity.Comment> page = commentRepo.findByApplication(app, pageable);
		return page.getContent().stream().map(comment -> { 
												Comment c = modelMapper.map(comment, Comment.class); 
												if(comment.getParent() != null)
													c.setParentId(comment.getParent().getId());
												if(comment.isDeleted()) 
													c.setComment("---"); 
												for(Comment sub : c.getSubComments()) {
													if(sub.isDeleted())
														sub.setComment("---");
												}
													
												return c;}
											).collect(Collectors.toList());
	}
	
	
	@RequestMapping(method=RequestMethod.POST)
	@Transactional
	public Id addComment(@PathVariable(value="appId", required=true) Long appId, @RequestBody(required=true) CommentRequest comment, Principal principal) throws MissingElementException, ProcessingException {
		Application app = getApp(appId);
		
		Long parentId = comment.getParentId();
		
		//Workaround problem of mapping parentId -> id
		//This should be fixed in modelmapper configuration
		comment.setParentId(null);
		net.geant.nmaas.portal.persistent.entity.Comment persistentComment = modelMapper.map(comment, net.geant.nmaas.portal.persistent.entity.Comment.class);
		if(persistentComment.getId() != null)
			throw new IllegalStateException("New comment cannot have id.");
		
		Optional<User> user = userRepo.findByUsername(principal.getName());
		if(!user.isPresent())
			throw new MissingElementException("User not found.");
		
		persistentComment.setApplication(app);
		persistentComment.setOwner(user.get());

//		if(persistentParentComment != null) 
//			commentRepo.save(persistentParentComment);
//		else
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

	@RequestMapping(value="/{commentId}", method=RequestMethod.POST)
	@Transactional
	public void editComment(@PathVariable(value="appId", required=true) Long appId, @PathVariable(value="commentId", required=true) Long commentId, @RequestBody(required=true) CommentRequest comment, Principal principal) throws MissingElementException, ProcessingException {
		throw new ProcessingException("Comment editing not supported.");
	}
	
	@RequestMapping(value="/{commentId}", method=RequestMethod.DELETE)
	@PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER') || hasPermission(#commentId, 'comment', 'owner')")
	@Transactional
	public void deleteComment(@PathVariable(value="appId") Long appId, @PathVariable(value="commentId") Long commentId) throws MissingElementException {
		net.geant.nmaas.portal.persistent.entity.Comment comment = getComment(commentId);
		comment.setDeleted(true);
		commentRepo.save(comment);
	}
	
	private net.geant.nmaas.portal.persistent.entity.Comment getComment(Long commentId) throws MissingElementException {
		if (commentId == null)
			throw new MissingElementException("Missing comment id." );
		net.geant.nmaas.portal.persistent.entity.Comment comment = commentRepo.findOne(commentId);
		if (comment == null)
			throw new MissingElementException("Comment id=" + commentId + " not found.");
		
		return comment;
	}
	
	
}
