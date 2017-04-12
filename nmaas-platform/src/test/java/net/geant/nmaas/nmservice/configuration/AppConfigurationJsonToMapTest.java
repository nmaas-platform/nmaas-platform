package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.orchestration.entities.AppConfiguration;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AppConfigurationJsonToMapTest {

    @Autowired
    private NmServiceConfigurationsPreparer configurationsPreparer;

    private AppConfiguration appConfiguration;

    @Before
    public void prepareAppConfiguration() {
        String jsonInput = "{\"routers\": [\"1.1.1.1\",\"2.2.2.2\"]}";
        appConfiguration = new AppConfiguration(jsonInput);
    }

    @Test
    public void shouldMapJsonToMap() throws IOException {
        List<String> list = (List<String>) configurationsPreparer.getModelFromJson(appConfiguration).get("routers");
        assertThat(list.size(), equalTo(2));
        assertThat(list, Matchers.contains("1.1.1.1", "2.2.2.2"));
    }

}

