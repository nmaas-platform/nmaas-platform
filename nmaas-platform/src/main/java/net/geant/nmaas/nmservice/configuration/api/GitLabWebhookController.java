package net.geant.nmaas.nmservice.configuration.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gitlab/webhooks")
public class GitLabWebhookController {

    @PostMapping("/{id}")
    public void triggerWebhook(@PathVariable Long id) {
        // TODO complete webhook handling
    }

}
