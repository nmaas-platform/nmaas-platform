package net.geant.nmaas.portal.api.apps;

import io.swagger.v3.oas.annotations.tags.Tag;
import net.geant.nmaas.api.dto.Id;
import net.geant.nmaas.api.dto.applications.CommentDto;
import net.geant.nmaas.api.dto.applications.CommentRequest;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Comment;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.persistence.repositories.CommentRepository;
import net.geant.nmaas.portal.persistence.repositories.UserRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import net.geant.nmaas.portal.service.ApplicationService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/apps/{appId}/comments")
@Tag(name = "Application Comments", description = "Operations related to application comments")
public class AppCommentsController extends AppBaseController {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Autowired
    public AppCommentsController(ModelMapper modelMapper,
                                 ApplicationService applicationService,
                                 ApplicationBaseService appBaseService,
                                 UserService userService,
                                 CommentRepository commentRepository,
                                 UserRepository userRepository) {
        super(modelMapper, userService, applicationService, appBaseService);
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'comment', 'READ')")
    public List<CommentDto> getComments(@PathVariable(value = "appId") Long appId, Pageable pageable) {
        ApplicationBase app = getBaseApp(appId);
        Page<Comment> page = commentRepository.findByApplication(app, pageable);
        return page.getContent().stream()
                .map(comment -> modelMapper.map(comment, CommentDto.class))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasPermission(null, 'comment', 'CREATE')")
    @Transactional
    public Id addComment(@PathVariable(value = "appId") Long appId, @RequestBody CommentRequest comment, Principal principal) {
        ApplicationBase app = getBaseApp(appId);

        if (comment.comment() == null || comment.comment().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        Long parentId = comment.parentId();

        // Workaround problem of mapping parentId -> id
        // This should be fixed in modelmapper configuration
        CommentRequest fixedCommentRequest = new CommentRequest(null, comment.comment());
        Comment persistentComment = modelMapper.map(comment, Comment.class);
        if (persistentComment.getId() != null) {
            throw new IllegalStateException("New comment cannot have id.");
        }

        User user = userRepository.findByUsername(principal.getName()).orElseThrow(() ->
                new MissingElementException("User not found."));

        persistentComment.setApplication(app);
        persistentComment.setOwner(user);

        Comment persistentParentComment;

        if (parentId != null) {
            // double check if parentId is null (above and inside `getComment` method)
            persistentParentComment = getComment(parentId);
            // below condition is redundant, because `getComment` method throws exception
            // when parent comment does not exist
            if (persistentParentComment == null)
                throw new MissingElementException("Unable to add comment to non-existing one");
            if (!persistentParentComment.getApplication().getId().equals(appId))
                throw new ProcessingException("Unable to add comment to different application");
            persistentComment.setParent(persistentParentComment);
        }
        commentRepository.save(persistentComment);

        return new Id(persistentComment.getId());
    }

    @PostMapping(value = "/{commentId}")
    @PreAuthorize("hasPermission(null, 'comment', 'WRITE')")
    @Transactional
    public void editComment(@PathVariable(value = "appId") Long appId, @PathVariable(value = "commentId", required = true) Long commentId, @RequestBody(required = true) CommentRequest comment, Principal principal) {
        throw new ProcessingException("Comment editing not supported.");
    }

    @DeleteMapping(value = "/{commentId}")
    @PreAuthorize("hasPermission(#commentId, 'comment', 'DELETE')")
    @Transactional
    public void deleteComment(@PathVariable String appId, @PathVariable(value = "commentId") Long commentId) {
        Comment comment = getComment(commentId);
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private Comment getComment(Long commentId) {
        if (commentId == null) {
            throw new MissingElementException("Missing comment id.");
        }
        return commentRepository.findById(commentId).orElseThrow(() -> new MissingElementException("Comment id=" + commentId + " not found."));
    }

}
