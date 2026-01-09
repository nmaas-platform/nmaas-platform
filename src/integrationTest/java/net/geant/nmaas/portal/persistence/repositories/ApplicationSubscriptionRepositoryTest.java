package net.geant.nmaas.portal.persistence.repositories;

import net.geant.nmaas.portal.persistence.entity.ApplicationBase;
import net.geant.nmaas.portal.persistence.entity.ApplicationSubscription;
import net.geant.nmaas.portal.persistence.entity.Domain;
import net.geant.nmaas.portal.persistence.entity.UsersHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class ApplicationSubscriptionRepositoryTest {

    @Autowired
    ApplicationBaseRepository appRepo;

    @Autowired
    DomainRepository domainRepo;

    @Autowired
    ApplicationSubscriptionRepository appSubRepo;

    private ApplicationBase app1, app2, app3;
    private Domain domain1, domain2;

    @BeforeEach
    void setUp() {
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

        domainRepo.findAll().stream()
                .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                .forEach(domain -> domainRepo.delete(domain));
        domain1 = domainRepo.save(new Domain("DOMAIN1", "D1", false));
        domain2 = domainRepo.save(new Domain("DOMAIN2", "D2", false));
        domainRepo.flush();

        appSubRepo.save(new ApplicationSubscription(domain1, app1, true));
        appSubRepo.save(new ApplicationSubscription(domain2, app1, false));
        appSubRepo.save(new ApplicationSubscription(domain1, app2, true));
        appSubRepo.flush();
    }

    @AfterEach
    void tearDown() {
        appSubRepo.deleteAll();
        appRepo.deleteAll();
        domainRepo.findAll().stream()
                .filter(domain -> !domain.getCodename().equalsIgnoreCase(UsersHelper.GLOBAL.getCodename()))
                .forEach(domain -> domainRepo.delete(domain));
    }

    @Test
    void testExistsDomainApplication() {
        assertTrue(appSubRepo.existsByDomainAndApplication(domain1, app1));
        assertFalse(appSubRepo.existsByDomainAndApplication(domain1, app3));
    }

    @Test
    void testFindOneDomainApplication() {
        assertTrue(appSubRepo.findByDomainAndApplication(domain1, app2).isPresent());
        assertFalse(appSubRepo.findByDomainAndApplication(domain1, app3).isPresent());
    }

    @Test
    void testFindApplicationBriefAll() {
        assertEquals(2, appSubRepo.findApplicationBriefAllBy().size());
    }

    @Test
    void testFindApplicationBriefAllByDomain() {
        assertEquals(1, appSubRepo.findApplicationBriefAllByDomain(domain2.getId()).size());
    }

}

