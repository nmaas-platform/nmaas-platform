package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.Domain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {PersistentConfig.class})
@EnableAutoConfiguration
@Transactional
@Rollback
public class ApplicationSubscriptionRepositoryTest {

	@Autowired
	private WebApplicationContext context;
	
	@Autowired
	ApplicationRepository appRepo;
	
	@Autowired
	DomainRepository domainRepo;
	
	@Autowired
	ApplicationSubscriptionRepository appSubRepo;
	
	Application app1, app2, app3;
	Domain domain1, domain2, domain3;
	ApplicationSubscription appSub1, appSub2, appSub3;
	
	@BeforeEach
	public void setUp() {

		app1 = appRepo.save(new Application("APP1","testversion","owner"));
		app2 = appRepo.save(new Application("APP2","testversion","owner"));
		app3 = appRepo.save(new Application("APP3","testversion","owner"));
		appRepo.flush();

		domain1 = domainRepo.save(new Domain("DOMAIN1", "D1",false,"testnamespace","teststorageclass"));
		domain2 = domainRepo.save(new Domain("DOMAIN2", "D2",false,"testnamespace","teststorageclass"));
		domain3 = domainRepo.save(new Domain("DOMAIN3", "D3",false,"testnamespace","teststorageclass"));
		domainRepo.flush();
		
		appSub1 = appSubRepo.save(new ApplicationSubscription(domain1, app1, true));
		appSub2 = appSubRepo.save(new ApplicationSubscription(domain2, app1, false));
		appSub3 = appSubRepo.save(new ApplicationSubscription(domain1, app2, true));
		appSubRepo.flush();
	}
	
	@AfterEach
	public void tearDown() {
		app1 = app2 = app3 = null;
		domain1 = domain2 = domain3 = null;
		appSub1 = appSub2 = appSub3 = null;
	}
	
	@Test
	public void testExistsDomainApplication() {
		assertTrue(appSubRepo.existsByDomainAndApplication(domain1, app1));
		assertFalse(appSubRepo.existsByDomainAndApplication(domain1, app3));
	}

	@Test
	public void testFindOneDomainApplication() {
		assertTrue(appSubRepo.findByDomainAndApplication(domain1, app2).isPresent());
		assertFalse(appSubRepo.findByDomainAndApplication(domain1, app3).isPresent());
	}

	@Test
	public void testFindAllByDomain() {
		assertEquals(2, appSubRepo.findAllByDomain(domain1, null).getContent().size());
		assertEquals(1, appSubRepo.findAllByDomain(domain2, null).getContent().size());
		assertEquals(0, appSubRepo.findAllByDomain(domain3, null).getContent().size());
	}

	@Test
	public void testFindAllByDomainWithActive() {
		assertEquals(2, appSubRepo.findAllByDomain(domain1, true, null).getContent().size());
		
		assertEquals(0, appSubRepo.findAllByDomain(domain2, true, null).getContent().size());
		assertEquals(1, appSubRepo.findAllByDomain(domain2, false, null).getContent().size());
	}
	
	
	@Test
	public void testFindAllByIdDomain() {
		assertEquals(2, appSubRepo.findAllByIdDomainAndActiveAndDeletedFalse(domain1, true).size());
		assertEquals(1, appSubRepo.findAllByIdDomainAndActiveAndDeletedFalse(domain2, false).size());
		assertEquals(0, appSubRepo.findAllByIdDomainAndActiveAndDeletedFalse(domain3, true).size());
	}

	@Test
	public void testFindAllByApplication() {
		assertEquals(2, appSubRepo.findAllByApplication(app1, null).getContent().size());
		assertEquals(1, appSubRepo.findAllByApplication(app2, null).getContent().size());
		assertEquals(0, appSubRepo.findAllByApplication(app3, null).getContent().size());
	}

	@Test
	public void testFindAllByApplicationWithActive() {
		assertEquals(1, appSubRepo.findAllByApplication(app1, true, null).getContent().size());
		
		assertEquals(1, appSubRepo.findAllByApplication(app1, false, null).getContent().size());
		assertEquals(1, appSubRepo.findAllByApplication(app2, true, null).getContent().size());
		
		assertEquals(0, appSubRepo.findAllByApplication(app3, true, null).getContent().size());
		assertEquals(0, appSubRepo.findAllByApplication(app3, false, null).getContent().size());
	}
	
	
	@Test
	public void testFindAllByIdApplication() {
		assertEquals(2, appSubRepo.findAllByApplication(app1, null).getContent().size());
		assertEquals(1, appSubRepo.findAllByApplication(app2, null).getContent().size());
		assertEquals(0, appSubRepo.findAllByApplication(app3, null).getContent().size());		
	}

	@Test
	public void testFindAllByIdApplicationWithActive() {
		assertEquals(1, appSubRepo.findAllByIdApplicationAndActive(app1, true, null).getContent().size());
		
		assertEquals(1, appSubRepo.findAllByIdApplicationAndActive(app1, false, null).getContent().size());
		assertEquals(1, appSubRepo.findAllByIdApplicationAndActive(app2, true, null).getContent().size());
		
		assertEquals(0, appSubRepo.findAllByIdApplicationAndActive(app3, true, null).getContent().size());
		assertEquals(0, appSubRepo.findAllByIdApplicationAndActive(app3, false, null).getContent().size());
	}
	
	@Test
	public void testFindApplicationBriefAll() {
		assertEquals(2, appSubRepo.findApplicationBriefAllBy().size());
	}
	
	@Test
	public void testFindApplicationBriefAllByDomain() {
		assertEquals(1, appSubRepo.findApplicationBriefAllByDomain(domain2.getId()).size());
	}
	
}
