package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Comment;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.service.DomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@Rollback
public class ApplicationRepositoryTest {
	
	@Autowired
	ApplicationRepository appRepo;
	
	@Autowired
	TagRepository tagRepo;
	
	@Autowired
	CommentRepository commentRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	DomainService domains;

	@Test
	@WithMockUser(username="admin", roles={"SYSTEM_ADMIN"})
	public void testAddApplication() {
		Application app1 = new Application("zabbix", "testversion", "owner");
		app1.setTags(new HashSet<>());
		app1.getTags().add(new Tag("monitoring1"));
		app1.getTags().add(new Tag("network1"));
		appRepo.save(app1);
		
		List<Application> apps = appRepo.findByName("zabbix");
		assertEquals(1, apps.size());
		app1 = apps.get(0);
		assertNotNull(app1.getId());
		
		Comment comment1 = new Comment(app1, "comment1");
		commentRepo.save(comment1);
		
		app1 = appRepo.findById(app1.getId()).get();
		Comment subComment1 = new Comment(app1, comment1, "comment2");
		commentRepo.save(subComment1);
		
		assertEquals(2, commentRepo.count());
	}

	@Test
	public void testTags() {
		Tag monitoringTag = new Tag("monitoring");
		monitoringTag.setApplications(new HashSet<>());
		monitoringTag = tagRepo.save(monitoringTag);
		monitoringTag = tagRepo.findByName("monitoring");

		Tag networkTag = new Tag("network");
		networkTag.setApplications(new HashSet<>());
		networkTag = tagRepo.save(networkTag);
		networkTag = tagRepo.findByName("network");
		
		Tag managementTag = tagRepo.save(new Tag("management"));
		managementTag.setApplications(new HashSet<>());
		managementTag = tagRepo.save(managementTag);
		managementTag = tagRepo.findByName("management");
		
		Application app1 = new Application("zabbix", "testversion", "owner");
		app1.setTags(new HashSet<>());
		app1.getTags().add(monitoringTag);
		monitoringTag.getApplications().add(app1);
		app1.getTags().add(networkTag);
		networkTag.getApplications().add(app1);
		appRepo.saveAndFlush(app1);

		Application app2 = new Application("librenms", "testversion", "owner");
		app2.setTags(new HashSet<>());
		app2.getTags().add(monitoringTag);
		monitoringTag.getApplications().add(app2);
		app2.getTags().add(managementTag);
		managementTag.getApplications().add(app2);
		app2.getTags().add(networkTag);
		networkTag.getApplications().add(app2);
		appRepo.saveAndFlush(app2);
		
		assertEquals(2, appRepo.count());
		assertEquals(3, tagRepo.count());

		assertEquals(2, appRepo.findByTags(monitoringTag).size());
		assertEquals(1, appRepo.findByTags(managementTag).size());

		assertNull(tagRepo.findByName("noexist"));
		assertEquals(2, tagRepo.findByName("monitoring").getApplications().size());
		assertEquals(1, tagRepo.findByName("management").getApplications().size());
	}
	
}
