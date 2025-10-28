package net.geant.nmaas;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.persistence.entity.Content;
import net.geant.nmaas.portal.persistence.repositories.ContentRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@DependsOn("portalConfiguration")
@Slf4j
public class DefaultContentInit implements InitializingBean {

    private final ContentRepository contentRepository;
    private final ResourceLoader resourceLoader;

    @Override
    @Transactional
    public void afterPropertiesSet() {
        log.info("[Init] Configuring default content (AUP and PP)");
        Optional<Content> defaultAcceptableUsePolicy = contentRepository.findByName("aup");
        if (defaultAcceptableUsePolicy.isEmpty()) {
            try {
                addContentToDatabase("aup", "Acceptable Use Policy", readContent("classpath:aup.txt"));
            } catch (IOException err) {
                throw new ProcessingException(err.getMessage());
            }
        }
        Optional<Content> defaultPrivacyPolicy = contentRepository.findByName("privacy");
        if (defaultPrivacyPolicy.isEmpty()) {
            try {
                addContentToDatabase("privacy", "Privacy Policy", readContent("classpath:privacy.txt"));
            } catch (IOException err) {
                throw new ProcessingException(err.getMessage());
            }
        }
    }

    private String readContent(String file) throws IOException {
        return new String(resourceLoader.getResource(file).getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void addContentToDatabase(String name, String title, String content) {
        Content newContent = new Content(name, title, content);
        contentRepository.save(newContent);
    }

}
