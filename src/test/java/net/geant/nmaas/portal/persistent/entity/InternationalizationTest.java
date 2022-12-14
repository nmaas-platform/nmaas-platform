package net.geant.nmaas.portal.persistent.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class InternationalizationTest {

    private InternationalizationView internationalization;

    private InternationalizationSimple internationalizationSimple;

    private static final String content = "{\n" +
                "  \"NAVBAR\": {\n" +
                "    \"MARKET\": \"Applications\",\n" +
                "    \"SUBSCRIPTIONS\": \"Subscriptions\",\n" +
                "    \"INSTANCES\": \"Instances\",\n" +
                "    \"MANAGEMENT\": \"Management\",\n" +
                "    \"DOMAINS\": \"Domains\",\n" +
                "    \"USERS\": \"Users\",\n" +
                "    \"INVENTORY\": \"External components\",\n" +
                "    \"MONITOR\": \"Monitoring\",\n" +
                "    \"SETTINGS\": \"Settings\",\n" +
                "    \"PROFILE\": \"Profile\",\n" +
                "    \"LOGOUT\": \"Logout\",\n" +
                "    \"LOGIN_REGISTER\": \"Login | Register\",\n" +
                "    \"LANGUAGE\": \"Language\",\n" +
                "    \"ABOUT\": \"About\",\n" +
                "    \"BACK\": \"Back to Home Page\",\n" +
                "    \"LANGUAGES\": \"Languages\"\n" +
                "  }\n" +
                "}";

    @BeforeEach
    public void setup(){
        this.internationalization = new InternationalizationView("english", true, content);
    }

    @Test
    public void internationalizationShouldDeserializeToInternationalizationSimple(){
        this.internationalizationSimple = this.internationalization.getAsInternationalizationSimple();
        assertTrue(StringUtils.isNotBlank(internationalization.getContent()));
//        assertEquals(internationalization.getId(), internationalizationSimple.getId());
        assertEquals(internationalization.getLanguage(), internationalizationSimple.getLanguage());
        assertEquals(internationalization.isEnabled(), internationalizationSimple.isEnabled());
        assertTrue(internationalizationSimple.getLanguageNodes().size() > 0);
    }

    @Test
    public void InternationalizationSimpleShouldSerializeToInternationalization(){
        this.internationalizationSimple = this.internationalization.getAsInternationalizationSimple();
        InternationalizationView test = this.internationalizationSimple.getAsInternationalizationView();
//        assertEquals(internationalization.getId(), test.getId());
        assertEquals(internationalization.getLanguage(), test.getLanguage());
        assertEquals(internationalization.isEnabled(), test.isEnabled());
        ObjectMapper om = new ObjectMapper();
        try{
            assertEquals(om.readTree(internationalization.getContent()), om.readTree(test.getContent()));
        } catch (IOException ioe){
            fail();
        }
    }

}
