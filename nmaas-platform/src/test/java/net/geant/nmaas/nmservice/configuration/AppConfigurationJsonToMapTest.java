package net.geant.nmaas.nmservice.configuration;

import net.geant.nmaas.nmservice.configuration.exceptions.UserConfigHandlingException;
import net.geant.nmaas.orchestration.entities.AppConfiguration;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
public class AppConfigurationJsonToMapTest {

    static final String EXAMPLE_OXIDIZED_CONFIG_FORM_INPUT =
    "{" +
        "\"oxidizedUsername\": \"testusername\"," +
        "\"oxidizedPassword\": \"testpassword\"," +
        "\"targets\": [" +
        "{" +
            "\"ipAddress\": \"1.1.1.1\"" +
        "}," +
        "{" +
            "\"ipAddress\": \"2.2.2.2\"" +
        "}]" +
    "}";

    static final String EXAMPLE_LIBRENMS_CONFIG_FORM_INPUT =
    "{" +
        "\"targets\": [" +
        "{" +
            "\"ipAddress\": \"192.168.1.1\"," +
            "\"snmpCommunity\": \"public\"," +
            "\"snmpVersion\": \"v2c\"" +
        "}," +
        "{" +
            "\"ipAddress\": \"10.10.3.2\"," +
            "\"snmpCommunity\": \"private\"," +
            "\"snmpVersion\": \"v2\"" +
        "}]" +
    "}";

    private NmServiceConfigurationFilePreparer configurationsPreparer = new NmServiceConfigurationFilePreparer();

    @Test
    public void shouldMapOxidizedJsonToMap() throws UserConfigHandlingException {
        AppConfiguration appConfiguration = new AppConfiguration(EXAMPLE_OXIDIZED_CONFIG_FORM_INPUT);
        List<Map> list = (List<Map>) configurationsPreparer.createModelFromJson(appConfiguration).get("targets");
        assertThat(list.size(), equalTo(2));
        assertThat(list.stream().map(entry -> entry.get("ipAddress")).collect(Collectors.toList()), Matchers.contains("1.1.1.1", "2.2.2.2"));
    }

    @Test
    public void shouldMapLibreNmsJsonToMap() throws UserConfigHandlingException {
        AppConfiguration appConfiguration = new AppConfiguration(EXAMPLE_LIBRENMS_CONFIG_FORM_INPUT);
        List<Map> list = (List<Map>) configurationsPreparer.createModelFromJson(appConfiguration).get("targets");
        assertThat(list.size(), equalTo(2));
        assertThat(list.stream().map(entry -> entry.get("ipAddress")).collect(Collectors.toList()), Matchers.contains("192.168.1.1", "10.10.3.2"));
    }

}

