package net.geant.nmaas.portal.persistent.repositories;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Comment;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.ApplicationRepository;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {PersistentConfig.class})
@EnableAutoConfiguration
@Transactional
@Rollback
public class ApplicationRepositoryTest {

	@Autowired
	private WebApplicationContext context;
	
	@Autowired
	ApplicationRepository appRepo;
	
	@Autowired
	TagRepository tagRepo;
	
	@Before
	public void setUp() throws Exception {
	
	}

	@Test
	public void testAddApplication() {

		Application app1 = new Application("zabbix");
		app1.setTags(new HashSet<Tag>());
		app1.getTags().add(new Tag("monitoring1"));
		app1.getTags().add(new Tag("network1"));
		appRepo.save(app1);
		
		List<Application> apps = appRepo.findByName("zabbix");
		assertEquals(1, apps.size());
		app1 = apps.get(0);
		assertNotNull(app1.getId());
		
		app1.getComments().add(new Comment(app1, "comment1"));
		appRepo.save(app1);
		
		app1 = appRepo.getOne(app1.getId());
		app1.getComments().add(new Comment(app1, app1.getComments().get(0), "comment2"));
		
		apps = appRepo.findAll();
		assertEquals(2, apps.get(0).getComments().size());
		
	}

	@Test
	public void testTags() {
		
		Tag monitoringTag = new Tag("monitoring");
		monitoringTag.setApplications(new HashSet<Application>());
		monitoringTag = tagRepo.save(monitoringTag);
		monitoringTag = tagRepo.findByName("monitoring");

		Tag networkTag = new Tag("network");
		networkTag.setApplications(new HashSet<Application>());
		networkTag = tagRepo.save(networkTag);
		networkTag = tagRepo.findByName("network");
		
		Tag managementTag = tagRepo.save(new Tag("management"));
		managementTag.setApplications(new HashSet<Application>());
		managementTag = tagRepo.save(managementTag);
		managementTag = tagRepo.findByName("management");
		
		Application app1 = new Application("zabbix");
		app1.setTags(new HashSet<Tag>());
		app1.getTags().add(monitoringTag);
		app1.getTags().add(networkTag);
		appRepo.saveAndFlush(app1);

		
		Application app2 = new Application("librenms");
		app2.setTags(new HashSet<Tag>());
		app2.getTags().add(monitoringTag);
		app2.getTags().add(managementTag);
		app2.getTags().add(networkTag);
		appRepo.saveAndFlush(app2);
		
		assertEquals(2, appRepo.count());
		assertEquals(3, tagRepo.count());

//		assertEquals(2, appRepo.findByTags(monitoringTag).size());
//		assertEquals(1, appRepo.findByTags(managementTag).size());

		assertNull(tagRepo.findByName("noexist"));
//		assertEquals(2, tagRepo.findByName("monitoring").getApplications().size());
//		assertEquals(1, tagRepo.findByName("management").getApplications().size());
		
		
	}
	
}
