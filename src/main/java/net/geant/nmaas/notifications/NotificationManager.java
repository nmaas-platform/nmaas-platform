package net.geant.nmaas.notifications;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
import net.geant.nmaas.portal.persistent.entity.Role;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
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
@RequiredArgsConstructor
public class NotificationManager {

    @Value("${portal.address}")
    private String portalAddress;

    @Setter
    @Value("${notifications.from-address}")
    private String fromAddress;

    private final TemplateService templateService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final DomainService domainService;
    private final ConfigurationManager configurationManager;
    private final ModelMapper modelMapper;
    private final FormTypeService formTypeService;

    /**
     * Main function of `NotificationManager`
     *
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

        for (UserView user : mailAttributes.getAddressees()) {
            try {
                LanguageMailContentView mailContent = getTemplateInSelectedLanguage(mailTemplate.getTemplates(), user.getSelectedLanguage());
                customizeMessage(mailContent, mailAttributes);
                String filledTemplate = getFilledTemplate(template, mailContent, user, mailAttributes, mailTemplate);
                if (Strings.isNullOrEmpty(fromAddress)) {
                    notificationService.sendMail(user.getEmail(), mailContent.getSubject(), filledTemplate);
                } else {
                    notificationService.sendMail(user.getEmail(), mailContent.getSubject(), filledTemplate, fromAddress);
                }
            } catch (TemplateException | IOException e) {
                log.error(String.format("Unable to generate template; to: [%s], template: [%s], message: %s", user.getEmail(), template.getName(), e.getMessage()));
            }
        }
        log.info("Mail " + mailAttributes.getMailType().name() + " was sent to " + getListOfMails(mailAttributes.getAddressees()));
    }

    private boolean isUserValidForBroadcastMail(UserView user) {
        if (user.getEmail().isEmpty()) {
            log.error(String.format("User: [%s] does not have an email address", user.getUsername()));
            return false;
        }
        return user.getRoles().stream().noneMatch(roleView -> roleView.getRole().equals(Role.ROLE_INCOMPLETE));
    }

    private LanguageMailContentView getTemplateInSelectedLanguage(List<LanguageMailContentView> mailContentList, String selectedLanguage) {
        return mailContentList.stream()
                .filter(mailContent -> mailContent.getLanguage().equalsIgnoreCase(selectedLanguage))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mail template in language " + selectedLanguage + " cannot be found"));
    }

    /**
     * This function sets email addresses server-side
     * When adding new MailType, make sure you edit this function so that your mail is sent to proper users
     * mailAttributes will be updated with new email addresses
     *
     * @param mailAttributes Mail attributes passed by the notification service client
     */
    private void getAllAddressees(MailAttributes mailAttributes) {
        if (mailAttributes.getMailType().equals(MailType.APP_DEPLOYMENT_FAILED)) {
            ConfigurationView configuration = this.configurationManager.getConfiguration();
            if (configuration.isSendAppInstanceFailureEmails()) {
                List<UserView> users = configuration.getAppInstanceFailureEmailList().stream()
                        .map(this::convertEmailToUserView)
                        .collect(Collectors.toList());
                mailAttributes.setAddressees(users);
            }
        }
        if (mailAttributes.getMailType().equals(MailType.EXTERNAL_SERVICE_HEALTH_CHECK)) {
            mailAttributes.setAddressees(userService.findUsersWithRoleSystemAdminAndOperator());
        }
        if (List.of(MailType.REGISTRATION, MailType.APP_NEW, MailType.NEW_SSO_LOGIN, MailType.APP_UPGRADE_SUMMARY)
                .contains(mailAttributes.getMailType())) {
            mailAttributes.setAddressees(userService.findAllUsersWithAdminRole());
        }
        if (List.of(MailType.APP_DEPLOYED, MailType.APP_UPGRADED, MailType.APP_UPGRADE_POSSIBLE)
                .contains(mailAttributes.getMailType())) {
            mailAttributes.setAddressees(domainService.findUsersWithDomainAdminRole((String)mailAttributes.getOtherAttributes().get("domainName")));
            if (mailAttributes.getAddressees().stream().noneMatch(user -> user.getUsername().equals(mailAttributes.getOtherAttributes().get("owner")))) {
                userService.findByUsername((String)mailAttributes.getOtherAttributes().get("owner"))
                        .ifPresent(user -> mailAttributes.getAddressees().add(modelMapper.map(user, UserView.class)));
            }
        }
        if (mailAttributes.getMailType().equals(MailType.BROADCAST)) {
            mailAttributes.setAddressees(userService.findAll().stream()
                    .filter(User::isEnabled)
                    .map(user -> modelMapper.map(user, UserView.class))
                    .filter(this::isUserValidForBroadcastMail)
                    .collect(Collectors.toList()));
        }
        if (List.of(MailType.CONTACT_FORM, MailType.ISSUE_REPORT, MailType.NEW_DOMAIN_REQUEST)
                .contains(mailAttributes.getMailType())) {
            List<UserView> base = userService.findAllUsersWithAdminRole();
            Optional<String> contactFormKey = Optional.ofNullable((String)mailAttributes.getOtherAttributes().get("subType"));
            if (contactFormKey.isEmpty()) {
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
     * This function handles message type specific logic e.g. custom title/subject for broadcast message
     *
     * @param mailContent    mail content to be customize
     * @param mailAttributes mail information and data provider
     */
    private void customizeMessage(LanguageMailContentView mailContent, MailAttributes mailAttributes) {
        if (mailAttributes.getMailType().equals(MailType.BROADCAST)) {
            mailContent.setSubject((String)mailAttributes.getOtherAttributes().getOrDefault(MailTemplateElements.TITLE, "NMAAS: Broadcast message")); //set subject from other params
        }
        if (mailAttributes.getMailType().equals(MailType.CONTACT_FORM)) {
            Optional<String> contactFormKey = Optional.ofNullable((String)mailAttributes.getOtherAttributes().get("subType"));
            Optional<FormType> formType = this.formTypeService.findOne(
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
                Map.of("username", user.getFirstname() == null || user.getFirstname().isEmpty() ? user.getUsername() : user.getFirstname()));
    }

    private String getContent(String content, Map<String, Object> otherAttributes) throws IOException, TemplateException {
        return FreeMarkerTemplateUtils.processTemplateIntoString(
                new Template(
                        MailTemplateElements.CONTENT,
                        new StringReader(content),
                        new Configuration(Configuration.VERSION_2_3_28)
                ),
                otherAttributes)
                .replace("\n", "<br/>"); // replace end line characters with html break
    }

    private List<String> getListOfMails(List<UserView> users) {
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