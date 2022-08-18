package net.geant.nmaas.portal.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.impl.crypto.DefaultSignerFactory;
import io.jsonwebtoken.impl.crypto.Signer;
import net.geant.nmaas.portal.api.security.SSOConfigManager;
import net.geant.nmaas.portal.api.BaseControllerTestSetup;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.i18n.api.InternationalizationView;
import net.geant.nmaas.portal.persistent.entity.UsersHelper;
import net.geant.nmaas.portal.persistent.repositories.UserRepository;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.InternationalizationService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SSOAuthControllerIntTest extends BaseControllerTestSetup {

    @Autowired
    private SSOConfigManager ssoConfigManager;

    @Autowired
    private ConfigurationManager configManager;

    @Autowired
    private InternationalizationService intService;

    @Autowired
    private UserRepository userRepo;

    @BeforeEach
    void setup() {
        this.mvc = createMVC();
        this.addLanguage();
        this.changeConfigToDefault();
    }

    @AfterEach
    void teardown() {
        this.userRepo.findAll().stream()
                .filter(user -> !user.getUsername().equalsIgnoreCase(UsersHelper.ADMIN.getUsername()))
                .forEach(user -> userRepo.delete(user));
        this.changeConfigToDefault();
    }

    @Test
    @Transactional
    void shouldRegister() throws Exception {
        MvcResult mvcResult = this.mvc.perform(post("/api/auth/sso/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(ImmutableMap.of("userid", getValidToken()))))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(StringUtils.isNotEmpty(mvcResult.getResponse().getContentAsString()));
        assertEquals(2, this.userRepo.count());
    }

    @Test
    @Transactional
    void shouldNotLoginWhenSSOIsDisabled() {
        ConfigurationView config = this.configManager.getConfiguration();
        config.setSsoLoginAllowed(false);
        config.setDefaultLanguage("en");
        assertDoesNotThrow(() -> {
            this.configManager.updateConfiguration(config.getId(), config);
            this.mvc.perform(post("/api/auth/sso/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(ImmutableMap.of("userid", getValidToken()))))
                    .andExpect(status().isConflict());
        });
    }

    @Test
    @Transactional
    void shouldNotLoginWhenUsernameIsEmpty() {
        String[] token = getValidToken().split("\\|");
        assertDoesNotThrow(() -> {
            this.mvc.perform(post("/api/auth/sso/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(ImmutableMap.of("userid", token[1] + token[2]))))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    @Transactional
    void shouldNotLoginWithInvalidSignature() {
        String[] token = getValidToken().split("\\|");
        token[2] = "invalidSignature";
        assertDoesNotThrow(() -> {
            this.mvc.perform(post("/api/auth/sso/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(ImmutableMap.of("userid", Arrays.toString(token)))))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    @Transactional
    void shouldNotLoginWithExpiredToken() {
        String[] token = getValidToken().split("\\|");
        token[1] = Long.toString((new Date().getTime() /1000) - 10000);
        assertDoesNotThrow(() -> {
            this.mvc.perform(post("/api/auth/sso/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(ImmutableMap.of("userid", Arrays.toString(token)))))
                    .andExpect(status().isBadRequest());
        });
    }

    @Test
    @Transactional
    void shouldNotLoginWithPortalMaintenance() {
        ConfigurationView config = this.configManager.getConfiguration();
        config.setMaintenance(true);
        config.setDefaultLanguage("en");
        assertDoesNotThrow(() -> {
            this.configManager.updateConfiguration(config.getId(), config);
            this.mvc.perform(post("/api/auth/sso/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(ImmutableMap.of("userid", getValidToken()))))
                    .andExpect(status().isNotAcceptable());
        });
    }

    private void addLanguage(){
        if(!intService.getEnabledLanguages().contains("en")) {
            intService.addNewLanguage(new InternationalizationView("en", true, "{\"content\":\"content\"}"));
        }
    }

    private void changeConfigToDefault() {
        ConfigurationView config = this.configManager.getConfiguration();
        config.setSsoLoginAllowed(true);
        config.setMaintenance(false);
        config.setDefaultLanguage("en");
        this.configManager.updateConfiguration(config.getId(), config);
    }

    private String getValidToken() {
        String signed = TextCodec.BASE64.encode("admin") + '|' + (new Date().getTime() / 1000) + 10000;
        byte[] keyBytes = ssoConfigManager.getKey().getBytes(StandardCharsets.US_ASCII);
        Key keyspec = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
        Signer signer = DefaultSignerFactory.INSTANCE.createSigner(SignatureAlgorithm.HS256, keyspec);
        String signature = DatatypeConverter.printHexBinary(signer.sign(signed.getBytes(StandardCharsets.US_ASCII)));
        return signed + '|' + signature;
    }
}
