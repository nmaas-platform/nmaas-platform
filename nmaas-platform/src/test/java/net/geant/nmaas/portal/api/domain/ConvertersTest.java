package net.geant.nmaas.portal.api.domain;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import net.geant.nmaas.portal.ConvertersConfig;
import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {ConvertersConfig.class, PersistentConfig.class})
@EnableAutoConfiguration
@Transactional
@Rollback
public class ConvertersTest {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    TagRepository tagRepo;
	
	@Before
	public void setUp() throws Exception {
        
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConvertApp() {
        tagRepo.save(new Tag("network"));
		
		ApplicationBrief appDto = null;
	    net.geant.nmaas.portal.persistent.entity.Application appEntity = null;
		

        appDto = new ApplicationBrief();
        appDto.setId(new Long(1));
        appDto.setBriefDescription("brief");
        appDto.setName("myApp");
        appDto.setVersion("version");
        appDto.setLicense("GNL");
        appDto.getTags().add("monitoring");
        appDto.getTags().add("network");

        appEntity = modelMapper.map(appDto, net.geant.nmaas.portal.persistent.entity.Application.class);

        assertEquals(appDto.getId(), appEntity.getId());
        assertEquals(appDto.getBriefDescription(), appEntity.getBriefDescription());
        assertEquals(appDto.getName(), appEntity.getName());
        assertEquals(appDto.getVersion(), appEntity.getVersion());
        assertEquals(appDto.getLicense(), appEntity.getLicense());
        assertEquals(2, appEntity.getTags().size());
        assertEquals(appDto.getTags().size(), appEntity.getTags().size());
        assertTrue((appEntity.getTags().toArray()[0]) instanceof Tag);

        Object[] tags = appEntity.getTags().toArray();

        assertNull( (((Tag)tags[0]).getName() == "monitoring" ? ((Tag)tags[0]).getId() : ((Tag)tags[1]).getId()));
        assertNotNull((((Tag)tags[1]).getName() == "network" ? ((Tag)tags[1]).getId() : ((Tag)tags[0]).getId()));

        appDto = modelMapper.map(appEntity, ApplicationBrief.class);
        assertEquals(2, appDto.getTags().size());
        assertEquals(appEntity.getTags().size(), appDto.getTags().size());
        assertTrue(appDto.getTags().contains("network"));
        assertTrue(appDto.getTags().contains("monitoring"));
  
	    
	}

}
