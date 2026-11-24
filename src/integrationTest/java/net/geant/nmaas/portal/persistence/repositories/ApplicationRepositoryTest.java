package net.geant.nmaas.portal.persistence.repositories;

import jakarta.transaction.Transactional;
import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.Comment;
import net.geant.nmaas.portal.persistence.entity.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
public class ApplicationRepositoryTest {

    @Autowired
    private ApplicationBaseRepository applicationBaseRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setup() {
        tagRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        applicationBaseRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"SYSTEM_ADMIN"})
    @Transactional
    void testAddApplication() {
        ApplicationBase app1 = new ApplicationBase("zabbix");
        app1.setOwner("admin");
        app1.setTags(new HashSet<>());
        app1.getTags().add(new Tag("monitoring1"));
        app1.getTags().add(new Tag("network1"));
        applicationBaseRepository.save(app1);

        app1 = applicationBaseRepository.findByName("zabbix").get();
        assertNotNull(app1.getId());

        Comment comment1 = new Comment(app1, "comment1");
        commentRepository.save(comment1);

        app1 = applicationBaseRepository.findById(app1.getId()).get();
        Comment subComment1 = new Comment(app1, comment1, "comment2");
        commentRepository.save(subComment1);

        assertEquals(2, commentRepository.count());
    }

    @Test
    @Transactional
    void testTags() {
        Tag monitoringTag = new Tag("monitoring");
        monitoringTag.setApplications(new HashSet<>());
        tagRepository.save(monitoringTag);
        monitoringTag = tagRepository.findByName("monitoring").get();

        Tag networkTag = new Tag("network");
        networkTag.setApplications(new HashSet<>());
        tagRepository.save(networkTag);
        networkTag = tagRepository.findByName("network").get();

        Tag managementTag = tagRepository.save(new Tag("management"));
        managementTag.setApplications(new HashSet<>());
        tagRepository.save(managementTag);
        managementTag = tagRepository.findByName("management").get();

        ApplicationBase app1 = new ApplicationBase("zabbix");
        app1.setOwner("admin");
        app1.setTags(new HashSet<>());
        app1.getTags().add(monitoringTag);
        monitoringTag.getApplications().add(app1);
        app1.getTags().add(networkTag);
        networkTag.getApplications().add(app1);
        applicationBaseRepository.saveAndFlush(app1);

        ApplicationBase app2 = new ApplicationBase("librenms");
        app2.setOwner("admin");
        app2.setTags(new HashSet<>());
        app2.getTags().add(monitoringTag);
        monitoringTag.getApplications().add(app2);
        app2.getTags().add(managementTag);
        managementTag.getApplications().add(app2);
        app2.getTags().add(networkTag);
        networkTag.getApplications().add(app2);
        applicationBaseRepository.saveAndFlush(app2);

        assertEquals(2, applicationBaseRepository.count());
        assertEquals(3, tagRepository.count());

        assertEquals(Optional.empty(), tagRepository.findByName("noexist"));
        assertEquals(2, tagRepository.findByName("monitoring").get().getApplications().size());
        assertEquals(1, tagRepository.findByName("management").get().getApplications().size());
    }

}
