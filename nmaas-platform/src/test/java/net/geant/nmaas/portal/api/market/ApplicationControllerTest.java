package net.geant.nmaas.portal.api.market;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import net.geant.nmaas.portal.BaseControllerTestSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback
public class ApplicationControllerTest extends BaseControllerTestSetup {
	
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
