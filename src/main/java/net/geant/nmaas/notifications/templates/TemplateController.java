package net.geant.nmaas.notifications.templates;

import lombok.RequiredArgsConstructor;
import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mail/templates")
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void addTemplate(@RequestBody MailTemplateView mailTemplate){
        this.templateService.saveMailTemplate(mailTemplate);
    }

    @PatchMapping("/all")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void updateTemplates(@RequestBody List<MailTemplateView> mailTemplates){
        mailTemplates.forEach(this.templateService::updateMailTemplate);
    }

    @PatchMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void updateTemplate(@RequestBody MailTemplateView mailTemplate){
        this.templateService.updateMailTemplate(mailTemplate);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @Transactional
    public List<MailTemplateView> getTemplates(){
        return this.templateService.getMailTemplates();
    }

    @PostMapping("/html")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void storeHtmlTemplate(@RequestBody MultipartFile file){
        templateService.storeHTMLTemplate(file);
    }

    @PatchMapping("/html")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    public void updateHtmlTemplate(@RequestBody MultipartFile file){
        this.templateService.updateHTMLTemplate(file);
    }
}
