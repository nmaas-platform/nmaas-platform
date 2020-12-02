package net.geant.nmaas.notifications;

import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.notifications.templates.TemplateService;
import net.geant.nmaas.notifications.templates.api.LanguageMailContentView;
import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import net.geant.nmaas.notifications.types.persistence.entity.FormType;
import net.geant.nmaas.notifications.types.service.FormTypeService;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.MissingElementException;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class handles notifications/emails sending logic
 */
@Service
@Log4j2
@AllArgsConstructor
public class NotificationManager {

    @Value("${portal.address}")
    private String portalAddress;

    private final TemplateService templateService;

    private final NotificationService notificationService;

    private final UserService userService;

    private final DomainService domainService;

    private ConfigurationManager configurationManager;

    private final ModelMapper modelMapper;

    private final FormTypeService formTypeService;

    @Autowired
    public NotificationManager(TemplateService templateService,
                               NotificationService notificationService,
                               UserService userService,
                               DomainService domainService,
                               ConfigurationManager configurationManager,
                               ModelMapper modelMapper,
                               FormTypeService formTypeService){
        this.templateService = templateService;
        this.notificationService = notificationService;
        this.userService = userService;
        this.domainService = domainService;
        this.configurationManager = configurationManager;
        this.modelMapper = modelMapper;
        this.formTypeService = formTypeService;
    }

    /**
     * Main function of `NotificationManager`
     * @param mailAttributes provided mail type and attributes
     */
    void prepareAndSendMail(MailAttributes mailAttributes) {
        MailTemplateView mailTemplate = templateService.getMailTemplate(mailAttributes.getMailType());

        Template template;
        try {
            template = templateService.getHTMLTemplate();
        } catch (IOException e) {
            log.error(String.format("Cannot retrieve html template: %s", e.getMessage()));
            throw new ProcessingException(e);
        }

        this.getAllAddressees(mailAttributes);

        for(UserView user : mailAttributes.getAddressees()){
            try {
                LanguageMailContentView mailContent = getTemplateInSelectedLanguage(mailTemplate.getTemplates(), user.getSelectedLanguage());
                this.customizeMessage(mailContent, mailAttributes);
                String filledTemplate = getFilledTemplate(template, mailContent, user, mailAttributes, mailTemplate);
                this.notificationService.sendMail(user.getEmail(), mailContent.getSubject(), filledTemplate);
            } catch (TemplateException | IOException e) {
                log.error(String.format("Unable to generate template; to: [%s], template: [%s], message: %s", user.getEmail(), template.getName(), e.getMessage()));
            }

        }
        log.info("Mail " + mailAttributes.getMailType().name() + " was sent to " + getListOfMails(mailAttributes.getAddressees()));
    }

    private LanguageMailContentView getTemplateInSelectedLanguage(List<LanguageMailContentView> mailContentList, String selectedLanguage){
        return mailContentList.stream()
                .filter(mailContent -> mailContent.getLanguage().equalsIgnoreCase(selectedLanguage))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mail template in language " + selectedLanguage + " cannot be found"));
    }

    /**
     * This function sets email addresses server-side
     * When adding new MailType, make sure you edit this function so that your mail is sent to proper users
     * mailAttributes will be updated with new email addresses
     * @param mailAttributes
     */
    private void getAllAddressees(MailAttributes mailAttributes){
        if(mailAttributes.getMailType().equals(MailType.APP_DEPLOYMENT_FAILED)){
            ConfigurationView configuration = this.configurationManager.getConfiguration();
            if(configuration.isSendAppInstanceFailureEmails()) {
                List<UserView> usrs = configuration.getAppInstanceFailureEmailList().stream()
                        .map(this::convertEmailToUserView)
                        .collect(Collectors.toList());
                mailAttributes.setAddressees(usrs);
            }
        }
        if(mailAttributes.getMailType().equals(MailType.EXTERNAL_SERVICE_HEALTH_CHECK)){
            mailAttributes.setAddressees(userService.findUsersWithRoleSystemAdminAndOperator());
        }
        if(mailAttributes.getMailType().equals(MailType.REGISTRATION)
                || mailAttributes.getMailType().equals(MailType.APP_NEW)
                || mailAttributes.getMailType().equals(MailType.NEW_SSO_LOGIN)
        ){
            mailAttributes.setAddressees(userService.findAllUsersWithAdminRole());
        }
        if(mailAttributes.getMailType().equals(MailType.APP_DEPLOYED)){
            mailAttributes.setAddressees(domainService.findUsersWithDomainAdminRole(mailAttributes.getOtherAttributes().get("domainName")));
            if(mailAttributes.getAddressees().stream().noneMatch(user -> user.getUsername().equals(mailAttributes.getOtherAttributes().get("owner")))){
                userService.findByUsername(mailAttributes.getOtherAttributes().get("owner"))
                        .ifPresent(user -> mailAttributes.getAddressees().add(modelMapper.map(user, UserView.class)));
            }
        }
        if(mailAttributes.getMailType().equals(MailType.BROADCAST)) {
            mailAttributes.setAddressees(userService.findAll().stream()
                    .filter(User::isEnabled)
                    .map(user -> modelMapper.map(user, UserView.class))
                    .collect(Collectors.toList()));
        }
        if(mailAttributes.getMailType().equals(MailType.CONTACT_FORM)) {
            List<UserView> base = userService.findAllUsersWithAdminRole();
            Optional<String> contactFormKey = Optional.ofNullable(mailAttributes.getOtherAttributes().get("subType"));
            if(!contactFormKey.isPresent()) {
                log.error("Invalid contact form request, subType is null");
            } else {
                this.formTypeService.findOne(contactFormKey.get())
                        .orElseThrow(() ->
                            new MissingElementException(
                                    String.format("Contact form type: [%s] was not found", contactFormKey.get())
                            )
                        )
                        .getEmailsList()
                        .forEach(email -> {
                            UserView userView = UserView.builder()
                                    .email(email)
                                    .username(email)
                                    .selectedLanguage("en")
                                    .build();
                            base.add(userView);
                        });
            }
            mailAttributes.setAddressees(base);
        }
    }

    /**
     * This function handles message type specific logic eg. custom title/subject for broadcast message
     * @param mailContent mail content to be customize
     * @param mailAttributes mail information and data provider
     */
    private void customizeMessage(LanguageMailContentView mailContent, MailAttributes mailAttributes) {
        if(mailAttributes.getMailType().equals(MailType.BROADCAST)) {
            mailContent.setSubject(mailAttributes.getOtherAttributes().getOrDefault(MailTemplateElements.TITLE, "NMAAS: Broadcast message")); //set subject from other params
        }
        if(mailAttributes.getMailType().equals(MailType.CONTACT_FORM)) {
            Optional<String> contactFormKey = Optional.ofNullable(mailAttributes.getOtherAttributes().get("subType"));
            Optional<FormType> formType =  this.formTypeService.findOne(
                    contactFormKey.orElseThrow(() -> new ProcessingException("Contact form subType not found"))
            );
            mailContent.setSubject(
                   formType.orElseThrow(() -> new MissingElementException(String.format("Contact form type: [%s] was not found", contactFormKey.get())))
                           .getSubject()
            );
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
        return FreeMarkerTemplateUtils.processTemplateIntoString(
                new Template(MailTemplateElements.HEADER, new StringReader(header), new Configuration(Configuration.VERSION_2_3_28)),
                ImmutableMap.of("username", user.getFirstname() == null || user.getFirstname().isEmpty() ? user.getUsername() : user.getFirstname()));
    }

    private String getContent(String content, Map<String, String> otherAttributes) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
                new Template(MailTemplateElements.CONTENT, new StringReader(content), new Configuration(Configuration.VERSION_2_3_28)),
                otherAttributes).replace("\n", "<br/>"); // replace end line characters with html break
    }

    private List<String> getListOfMails(List<UserView> users){
        return users.stream()
                .map(UserView::getEmail)
                .collect(Collectors.toList());
    }

    private UserView convertEmailToUserView(String email) {
        try {
            return modelMapper.map(this.userService.findByEmail(email), UserView.class);
        } catch (IllegalArgumentException e) {
            UserView uv = new UserView(-1L, email, false);
            uv.setEmail(email);
            uv.setSelectedLanguage("en");
            return uv;
        }
    }

}