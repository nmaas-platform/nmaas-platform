package net.geant.nmaas.nmservice.configuration.api;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/gitlab/webhooks")
public class GitLabWebhookController {

    @PostMapping("/{id}")
    public void triggerWebhook(@PathVariable String id) {
        // TODO complete webhook handling
        log.info("Triggered webhook with id: " + id);
    }

}
