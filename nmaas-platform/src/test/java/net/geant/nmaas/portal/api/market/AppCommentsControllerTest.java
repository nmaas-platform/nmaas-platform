package net.geant.nmaas.portal.api.market;

import net.geant.nmaas.portal.api.domain.CommentRequest;
import net.geant.nmaas.portal.api.domain.CommentView;
import net.geant.nmaas.portal.api.domain.Id;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.Comment;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.persistent.repositories.CommentRepository;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.ApplicationBaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AppCommentsControllerTest {

    private ApplicationBaseService applicationBaseService = mock(ApplicationBaseService.class);
    private UserRepository userRepository = mock(UserRepository.class);
    private CommentRepository commentRepository = mock(CommentRepository.class);

    private AppCommentsController appCommentsController;

    private ApplicationBase app;

    private User user;

    @BeforeEach
    public void setup() {

        app = new ApplicationBase(1L, "name");
        when(applicationBaseService.findByName("name")).thenReturn(app);
        when(applicationBaseService.getBaseApp(1L)).thenReturn(app);

        user = new User("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        this.appCommentsController = new AppCommentsController(commentRepository, userRepository);
        this.appCommentsController.appBaseService = applicationBaseService;
        this.appCommentsController.modelMapper = new ModelMapper();

    }

    @Test
    public void shouldAddComment() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());

        CommentRequest cr = new CommentRequest();
        cr.setParentId(null);
        cr.setComment("Test comment");

        Id result = this.appCommentsController.addComment(app.getId(), cr, principal);

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void shouldNotAddEmptyComment() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());

        CommentRequest cr = new CommentRequest();
        cr.setParentId(null);
        cr.setComment("");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            this.appCommentsController.addComment(app.getId(), cr, principal);
        });

        assertEquals("Comment cannot be empty", e.getMessage());

        CommentRequest cr2 = new CommentRequest();
        cr2.setParentId(null);
        cr2.setComment(null);

        e = assertThrows(IllegalArgumentException.class, () -> {
            this.appCommentsController.addComment(app.getId(), cr2, principal);
        });

        assertEquals("Comment cannot be empty", e.getMessage());
    }

    @Test
    public void shouldAddCommentWithValidParentComment() {
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());

        Comment parent = new Comment(app,"Parent", user);
        Long parentId = 2137L;
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parent));

        CommentRequest cr = new CommentRequest();
        cr.setParentId(parentId);
        cr.setComment("Child comment");

        Id result = this.appCommentsController.addComment(app.getId(), cr, principal);

        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    public void shouldThrowExceptionWhenParentCommentNotFound() {
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
    public void shouldThrowExceptionWhenParentCommentBelongsToAnotherApplication() {
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
    public void shouldGetAllCommentsByApp() {
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

        when(commentRepository.findByApplication(app, any(Pageable.class))).thenReturn(commentPage);

        List<CommentView> result = this.appCommentsController.getComments(app.getId(), null);
        assertEquals(3, result.size());
    }
}
