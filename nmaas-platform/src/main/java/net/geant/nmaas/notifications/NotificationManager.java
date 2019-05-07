package net.geant.nmaas.notifications;

import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringReader;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.notifications.templates.api.LanguageMailContentView;
import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.notifications.templates.TemplateService;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
@Log4j2
public class NotificationManager {

    @Value("${portal.address}")
    private String portalAddress;

    private TemplateService templateService;

    private NotificationService notificationService;

    private ConfigurationManager configurationManager;

    private UserService userService;

    private DomainService domainService;

    private ModelMapper modelMapper;

    @Autowired
    public NotificationManager(TemplateService templateService,
                               NotificationService notificationService,
                               ConfigurationManager configurationManager,
                               UserService userService,
                               DomainService domainService,
                               ModelMapper modelMapper){
        this.templateService = templateService;
        this.notificationService = notificationService;
        this.configurationManager = configurationManager;
        this.userService = userService;
        this.domainService = domainService;
        this.modelMapper = modelMapper;
    }

    void prepareAndSendMail(MailAttributes mailAttributes) throws IOException, TemplateException {
        MailTemplateView mailTemplate = templateService.getMailTemplate(mailAttributes.getMailType());
        Template template = templateService.getHTMLTemplate();
        LanguageMailContentView langTemplate = mailTemplate.getTemplates().stream()
                .filter(temp -> temp.getLanguage().equals(configurationManager.getConfiguration().getDefaultLanguage()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mail template not found"));
        this.getAllAddressees(mailAttributes);
        for(UserView user : mailAttributes.getAddressees()){
            this.notificationService.sendMail(user.getEmail(), langTemplate.getSubject(), getFilledTemplate(template, langTemplate, user, mailAttributes, mailTemplate));
        }
        log.info("Mail " + mailAttributes.getMailType().name() + " was sent to " + getListOfMails(mailAttributes.getAddressees()));
    }

    private void getAllAddressees(MailAttributes mailAttributes){
        if(mailAttributes.getMailType().equals(MailType.EXTERNAL_SERVICE_HEALTH_CHECK)){
            mailAttributes.setAddressees(userService.findUsersWithRoleSystemAdminAndOperator());
        }
        if(mailAttributes.getMailType().equals(MailType.REGISTRATION) || mailAttributes.getMailType().equals(MailType.APP_NEW) || mailAttributes.getMailType().equals(MailType.CONTACT_FORM)){
            mailAttributes.setAddressees(userService.findAllUsersWithAdminRole());
        }
        if(mailAttributes.getMailType().equals(MailType.APP_DEPLOYED)){
            mailAttributes.setAddressees(domainService.findUsersWithDomainAdminRole(mailAttributes.getOtherAttributes().get("domainName")));
            if(mailAttributes.getAddressees().stream().noneMatch(user -> user.getUsername().equals(mailAttributes.getOtherAttributes().get("owner")))){
                userService.findByUsername(mailAttributes.getOtherAttributes().get("owner"))
                        .ifPresent(user -> mailAttributes.getAddressees().add(modelMapper.map(user, UserView.class)));
            }
        }
    }

    private String getFilledTemplate(Template template, LanguageMailContentView langContent, UserView user, MailAttributes mailAttributes, MailTemplateView mailTemplate) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, ImmutableMap.builder()
                .putAll(mailTemplate.getGlobalInformation())
                .put(MailTemplateElements.PORTAL_LINK, this.portalAddress == null ? "" : this.portalAddress)
                .put(MailTemplateElements.HEADER, getHeader(langContent.getTemplate().get(MailTemplateElements.HEADER), user))
                .put(MailTemplateElements.CONTENT, getContent(langContent.getTemplate().get(MailTemplateElements.CONTENT), mailAttributes.getOtherAttributes()))
                .put(MailTemplateElements.SENDER, langContent.getTemplate().get(MailTemplateElements.SENDER))
                .put(MailTemplateElements.NOREPLY, langContent.getTemplate().get(MailTemplateElements.NOREPLY))
                .put(MailTemplateElements.SENDER_POLICY, langContent.getTemplate().get(MailTemplateElements.SENDER_POLICY))
                .put(MailTemplateElements.TITLE, langContent.getSubject())
                .build());
    }

    private String getHeader(String header, UserView user) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(new Template(MailTemplateElements.HEADER, new StringReader(header), new Configuration(Configuration.VERSION_2_3_28)), ImmutableMap.of("username", user.getFirstname() == null || user.getFirstname().isEmpty() ? user.getUsername() : user.getFirstname()));
    }

    private String getContent(String content, Map<String, String> otherAttributes) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(new Template(MailTemplateElements.CONTENT, new StringReader(content), new Configuration(Configuration.VERSION_2_3_28)), otherAttributes);
    }

    private List<String> getListOfMails(List<UserView> users){
        return users.stream()
                .map(UserView::getEmail)
                .collect(Collectors.toList());
    }

}