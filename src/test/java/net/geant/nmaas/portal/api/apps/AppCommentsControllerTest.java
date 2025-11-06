package net.geant.nmaas.portal.api.apps;

import net.geant.nmaas.portal.domain.CommentRequest;
import net.geant.nmaas.portal.domain.CommentView;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppCommentsControllerTest {

    private final ApplicationService applicationService = mock(ApplicationService.class);
    private final ApplicationBaseService applicationBaseService = mock(ApplicationBaseService.class);
    private final UserService userService = mock(UserService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CommentRepository commentRepository = mock(CommentRepository.class);

    private AppCommentsController appCommentsController;

    private ApplicationBase app;

    private User user;

    @BeforeEach
    void setup() {
        app = new ApplicationBase(1L, "name");
        when(applicationBaseService.findByName("name")).thenReturn(app);
        when(applicationBaseService.getBaseApp(1L)).thenReturn(app);

        user = new User("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        this.appCommentsController = new AppCommentsController(
                new ModelMapper(),
                applicationService,
                applicationBaseService,
                userService,
                commentRepository,
                userRepository);
    }

    @Test
    void shouldAddComment() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());

        CommentRequest cr = new CommentRequest();
        cr.setParentId(null);
        cr.setComment("Test comment");

        appCommentsController.addComment(app.getId(), cr, principal);

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void shouldNotAddEmptyComment() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());

        CommentRequest cr = new CommentRequest();
        cr.setParentId(null);
        cr.setComment("");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            appCommentsController.addComment(app.getId(), cr, principal);
        });

        assertEquals("Comment cannot be empty", e.getMessage());

        CommentRequest cr2 = new CommentRequest();
        cr2.setParentId(null);
        cr2.setComment(null);

        e = assertThrows(IllegalArgumentException.class, () -> {
            appCommentsController.addComment(app.getId(), cr2, principal);
        });

        assertEquals("Comment cannot be empty", e.getMessage());
    }

    @Test
    void shouldAddCommentWithValidParentComment() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());

        Comment parent = new Comment(app,"Parent", user);
        Long parentId = 2137L;
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parent));

        CommentRequest cr = new CommentRequest();
        cr.setParentId(parentId);
        cr.setComment("Child comment");

        this.appCommentsController.addComment(app.getId(), cr, principal);

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void shouldThrowExceptionWhenParentCommentNotFound() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());

        Long parentId = 2137L;
        when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

        CommentRequest cr = new CommentRequest();
        cr.setParentId(parentId);
        cr.setComment("Child comment");

        MissingElementException me = assertThrows(MissingElementException.class, () -> {
            this.appCommentsController.addComment(app.getId(), cr, principal);
        });

        assertTrue(me.getMessage().contains(String.valueOf(parentId)));
    }

    @Test
    void shouldThrowExceptionWhenParentCommentBelongsToAnotherApplication() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());

        ApplicationBase otherApp = new ApplicationBase(14L, "other");

        Comment parent = new Comment(otherApp,"Parent", user);
        Long parentId = 2137L;
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parent));

        CommentRequest cr = new CommentRequest();
        cr.setParentId(parentId);
        cr.setComment("Child comment");

        ProcessingException me = assertThrows(ProcessingException.class, () -> {
            this.appCommentsController.addComment(app.getId(), cr, principal);
        });

        assertEquals("Unable to add comment to different application", me.getMessage());
    }

    @Test
    void shouldGetAllCommentsByApp() {
        Comment c1 = new Comment(app, "Root comment", user);
        Comment c2 = new Comment(app, "Deleted comment", user);
        c2.setDeleted(true);
        Comment c3 = new Comment(app, "Parent comment", user);
        Comment c31 = new Comment(app, "Sub comment 1", user);
        Comment c32 = new Comment(app, "Sub comment 2", user);
        List<Comment> subComments = new ArrayList<>();
        subComments.add(c31);
        subComments.add(c32);
        c3.setSubComments(subComments);
        List<Comment> mainComments = new ArrayList<>();
        mainComments.add(c1);
        mainComments.add(c2);
        mainComments.add(c3);

        Page<Comment> commentPage = new PageImpl<>(mainComments);

        when(commentRepository.findByApplication(app, null)).thenReturn(commentPage);

        List<CommentView> result = this.appCommentsController.getComments(app.getId(), null);
        assertEquals(3L, result.size());
    }

}
