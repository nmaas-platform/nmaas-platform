package net.geant.nmaas.portal.persistent.repositories;

import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.ApplicationBase;
import net.geant.nmaas.portal.persistent.entity.ApplicationState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ApplicationBaseRepositoryTest {

    @Autowired
    private ApplicationBaseRepository repository;

    @Autowired
    private ApplicationRepository applicationRepository;

    private static final String APP1_NAME = "app1";
    private static final String APP2_NAME = "app2";
    private static final String APP3_NAME = "app3";

    @AfterEach
    public void tearDown() {
        repository.deleteAll();
        applicationRepository.deleteAll();
    }

    @Test
    public void shouldQueryApplicationBaseData() {
        ApplicationBase appBase1 = new ApplicationBase();
        appBase1.setName(APP1_NAME);
        repository.save(appBase1);
        Application app1 = new Application();
        app1.setName(APP1_NAME);
        app1.setState(ApplicationState.ACTIVE);
        app1.setVersion("1");
        app1.setOwner("owner");
        app1.setCreationDate(LocalDateTime.now());
        applicationRepository.save(app1);
        ApplicationBase appBase2 = new ApplicationBase();
        appBase2.setName(APP2_NAME);
        repository.save(appBase2);
        Application app2 = new Application();
        app2.setName(APP2_NAME);
        app2.setState(ApplicationState.DISABLED);
        app2.setVersion("1");
        app2.setOwner("owner");
        app2.setCreationDate(LocalDateTime.now());
        applicationRepository.save(app2);
        Application app3 = new Application();
        app3.setName(APP2_NAME);
        app3.setState(ApplicationState.ACTIVE);
        app3.setVersion("2");
        app3.setOwner("owner");
        app3.setCreationDate(LocalDateTime.now());
        applicationRepository.save(app3);
        ApplicationBase appBase3 = new ApplicationBase();
        appBase3.setName(APP3_NAME);
        repository.save(appBase3);
        Application app4 = new Application();
        app4.setName(APP3_NAME);
        app4.setState(ApplicationState.DISABLED);
        app4.setVersion("1");
        app4.setOwner("owner");
        app4.setCreationDate(LocalDateTime.now());
        applicationRepository.save(app4);

        assertThat(repository.count(), is(3L));
        assertThat(repository.countAllActive(), is(2L));
        assertThat(repository.findAllNames(), contains(APP1_NAME, APP2_NAME, APP3_NAME));
        assertTrue(repository.existsByName(APP1_NAME));
    }

}
