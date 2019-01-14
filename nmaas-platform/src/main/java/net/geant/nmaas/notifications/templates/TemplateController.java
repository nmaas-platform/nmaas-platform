package net.geant.nmaas.notifications.templates;

import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail/templates")
public class TemplateController {

    private TemplateService templateService;

    @Autowired
    public TemplateController(TemplateService templateService){
        this.templateService = templateService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addTemplate(@RequestBody MailTemplateView mailTemplate){
        this.templateService.saveMailTemplate(mailTemplate);
    }

    @PatchMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void updateTemplate(@RequestBody MailTemplateView mailTemplate){
        this.templateService.updateMailTemplate(mailTemplate);
    }
}
