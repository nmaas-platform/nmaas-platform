package net.geant.nmaas.portal.api.market;

import static org.junit.Assert.*;

import javax.servlet.Filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import net.geant.nmaas.portal.BaseControllerTest;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class ApplicationControllerTest extends BaseControllerTest {
	
	@Before
	public void setUp() throws Exception {
        mvc = createMVC();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
	}

}
