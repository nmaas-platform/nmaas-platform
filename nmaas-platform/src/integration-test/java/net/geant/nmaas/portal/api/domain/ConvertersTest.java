package net.geant.nmaas.portal.api.domain;

import net.geant.nmaas.portal.ConvertersConfig;
import net.geant.nmaas.portal.PersistentConfig;
import net.geant.nmaas.portal.persistent.entity.Application;
import net.geant.nmaas.portal.persistent.entity.Tag;
import net.geant.nmaas.portal.persistent.repositories.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


@ExtendWith(SpringExtension.class)
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
	
	@Test
	public void testConvertApp() {
        tagRepo.save(new Tag("network"));
		
		ApplicationBriefView appDto = null;
	    Application appEntity = null;

        appDto = new ApplicationBriefView();
        appDto.setId(1L);
        appDto.setName("myApp");
        appDto.setVersion("version");
        appDto.setLicense("GNL");
        appDto.getTags().add("monitoring");
        appDto.getTags().add("network");

        appEntity = modelMapper.map(appDto, Application.class);

        assertEquals(appDto.getId(), appEntity.getId());
        assertEquals(appDto.getName(), appEntity.getName());
        assertEquals(appDto.getVersion(), appEntity.getVersion());
        assertEquals(appDto.getLicense(), appEntity.getLicense());
        assertEquals(2, appEntity.getTags().size());
        assertEquals(appDto.getTags().size(), appEntity.getTags().size());
        assertTrue((appEntity.getTags().toArray()[0]) instanceof Tag);

        Object[] tags = appEntity.getTags().toArray();

        assertNull( (((Tag) tags[0]).getName().equals("monitoring") ? ((Tag)tags[0]).getId() : ((Tag)tags[1]).getId()));
        assertNotNull((((Tag) tags[1]).getName().equals("network") ? ((Tag)tags[1]).getId() : ((Tag)tags[0]).getId()));

        appDto = modelMapper.map(appEntity, ApplicationBriefView.class);
        assertEquals(2, appDto.getTags().size());
        assertEquals(appEntity.getTags().size(), appDto.getTags().size());
        assertTrue(appDto.getTags().contains("network"));
        assertTrue(appDto.getTags().contains("monitoring"));
  
	    
	}

}
