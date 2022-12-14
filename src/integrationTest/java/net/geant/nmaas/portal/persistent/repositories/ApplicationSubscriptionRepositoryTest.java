package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistent.entity.Domain;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ApplicationSubscriptionRepositoryTest {
	
	@Autowired
	ApplicationBaseRepository appRepo;
	
	@Autowired
	DomainRepository domainRepo;
	
	@Autowired
	ApplicationSubscriptionRepository appSubRepo;
	
	private ApplicationBase app1, app2, app3;
	private Domain domain1, domain2, domain3;
	private ApplicationSubscription appSub1, appSub2, appSub3;
	
	@BeforeEach
	public void setUp() {

		app1 = new ApplicationBase("APP1");
		app1.setOwner("");
		app2 = new ApplicationBase("APP2");
		app2.setOwner("");
		app3 = new ApplicationBase("APP3");
		app3.setOwner("");

		app1 = appRepo.save(app1);
		app2 = appRepo.save(app2);
		app3 = appRepo.save(app3);
		appRepo.flush();

		domain1 = domainRepo.save(new Domain("DOMAIN1", "D1",false));
		domain2 = domainRepo.save(new Domain("DOMAIN2", "D2",false));
		domain3 = domainRepo.save(new Domain("DOMAIN3", "D3",false));
		domainRepo.flush();
		
		appSub1 = appSubRepo.save(new ApplicationSubscription(domain1, app1, true));
		appSub2 = appSubRepo.save(new ApplicationSubscription(domain2, app1, false));
		appSub3 = appSubRepo.save(new ApplicationSubscription(domain1, app2, true));
		appSubRepo.flush();
	}
	
	@AfterEach
	public void tearDown() {
		appSubRepo.deleteAll();
		appRepo.deleteAll();
		domainRepo.findAll().stream()
				.filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
				.forEach(domain -> domainRepo.delete(domain));
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
