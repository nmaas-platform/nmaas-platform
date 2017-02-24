package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.orchestration.AppConfiguration;
import net.geant.nmaas.orchestration.Identifier;
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
    private SimpleNmServiceConfigurationExecutor configurationExecutor;

    private AppConfiguration appConfiguration;

    @Before
    public void prepareAppConfiguration() {
        Identifier applicationId = Identifier.newInstance("appId1");
        String jsonInput = "{\"routers\": [\"1.1.1.1\",\"2.2.2.2\"]}";
        appConfiguration = new AppConfiguration(applicationId, jsonInput);
    }

    @Test
    public void shouldMapJsonToMap() throws IOException {
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) configurationExecutor.getModelFromJson(appConfiguration).get("routers");
        assertThat(list.size(), equalTo(2));
        System.out.println(list.toString());
        assertThat(list, Matchers.contains("1.1.1.1", "2.2.2.2"));
    }

}

