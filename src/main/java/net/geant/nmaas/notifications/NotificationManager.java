package net.geant.nmaas.notifications;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.notifications.templates.TemplateService;
import net.geant.nmaas.notifications.templates.api.LanguageMailContentView;
import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import net.geant.nmaas.notifications.types.persistence.entity.FormType;
import net.geant.nmaas.notifications.types.service.FormTypeService;
import net.geant.nmaas.portal.api.configuration.model.ConfigurationView;
import net.geant.nmaas.portal.api.exceptions.MissingElementException;
import net.geant.nmaas.portal.api.exceptions.ProcessingException;
import net.geant.nmaas.portal.domain.GroupAppListElement;
import net.geant.nmaas.portal.domain.UserView;
import net.geant.nmaas.portal.persistence.entity.Role;
import net.geant.nmaas.portal.persistence.entity.User;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class handles notifications/emails sending logic
 */
@Service
@Slf4j
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
            log.error("Cannot retrieve html template: {}", e.getMessage());
            throw new ProcessingException(e);
        }

        this.getAllAddressees(mailAttributes);

        for (UserView user : mailAttributes.getAddresses()) {
            try {
                LanguageMailContentView mailContent = getTemplateInSelectedLanguage(mailTemplate.getTemplates(), user.getSelectedLanguage());
                customizeMessage(mailContent, mailAttributes);
                String filledTemplate = getFilledTemplate(template, mailContent, user, mailAttributes, mailTemplate);
                if (StringUtils.isEmpty(fromAddress)) {
                    notificationService.sendMail(user.getEmail(), mailContent.getSubject(), filledTemplate);
                } else {
                    notificationService.sendMail(user.getEmail(), mailContent.getSubject(), filledTemplate, fromAddress);
                }
            } catch (TemplateException | IOException e) {
                log.error("Unable to generate template; to: [{}], template: [{}], message: {}", user.getEmail(), template.getName(), e.getMessage());
            }
        }
        log.info("Mail {} was sent to {}", mailAttributes.getMailType().name(), getListOfMails(mailAttributes.getAddresses()));
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
                mailAttributes.setAddresses(users);
            }
        }
        if (mailAttributes.getMailType().equals(MailType.EXTERNAL_SERVICE_HEALTH_CHECK)) {
            mailAttributes.setAddresses(userService.findUsersWithRoleSystemAdminAndOperator());
        }
        if (mailAttributes.getMailType().equals(MailType.NEW_BULK_LOGIN) || mailAttributes.getMailType().equals(MailType.NEW_BULK_SSO_LOGIN)) {
            mailAttributes.setAddresses(List.of(convertEmailToUserView((mailAttributes.getOtherAttributes().get("email").toString()))));
        }
        if (List.of(MailType.REGISTRATION, MailType.APP_NEW, MailType.NEW_SSO_LOGIN, MailType.APP_UPGRADE_SUMMARY)
                .contains(mailAttributes.getMailType())) {
            mailAttributes.setAddresses(userService.findAllUsersWithAdminRole());
        }
        if (List.of(MailType.APP_DEPLOYED, MailType.APP_UPGRADED, MailType.APP_UPGRADE_POSSIBLE)
                .contains(mailAttributes.getMailType())) {
            mailAttributes.setAddresses(new ArrayList<>(domainService.findUsersWithDomainAdminRole((String) mailAttributes.getOtherAttributes().get("domainName"))));
            if (mailAttributes.getAddresses().stream().noneMatch(user -> user.getUsername().equals(mailAttributes.getOtherAttributes().get("owner")))) {
                userService.findByUsername((String) mailAttributes.getOtherAttributes().get("owner"))
                        .ifPresent(user -> mailAttributes.getAddresses().add(modelMapper.map(user, UserView.class)));
            }
        }
        if (mailAttributes.getMailType().equals(MailType.BROADCAST)) {
            mailAttributes.setAddresses(userService.findAll().stream()
                    .filter(User::isEnabled)
                    .filter(u -> !StringUtils.isEmpty(u.getEmail()))
                    .filter(u -> u.getRoles().stream().noneMatch(r -> r.getRole().equals(Role.ROLE_INCOMPLETE)))
                    .map(u -> modelMapper.map(u, UserView.class))
                    .collect(Collectors.toList()));
        }
        if (List.of(MailType.CONTACT_FORM, MailType.ISSUE_REPORT, MailType.NEW_DOMAIN_REQUEST, MailType.VLAB_REQUEST)
                .contains(mailAttributes.getMailType())) {
            Optional<String> contactFormKey = Optional.ofNullable((String) mailAttributes.getOtherAttributes().get("subType"));
            if (mailAttributes.getMailType().equals(MailType.VLAB_REQUEST)) {
                Object datesObject = mailAttributes.getOtherAttributes().get("dates");
                ObjectMapper objectMapper = new ObjectMapper();
                List<Object> datesList = objectMapper.convertValue(datesObject, new TypeReference<List<Object>>() {
                });
                Map<String, Object> dates = objectMapper.convertValue(datesList.get(0), new TypeReference<Map<String, Object>>() {
                });

                dates.forEach((k, v) -> {
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(v.toString());
                    mailAttributes.getOtherAttributes().put(k, offsetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                });

                List<GroupAppListElement> appList = objectMapper.convertValue(mailAttributes.getOtherAttributes().get("appList"), new TypeReference<List<GroupAppListElement>>() {});
                mailAttributes.getOtherAttributes().put("appList", appList.stream().map(GroupAppListElement::getAppListName).collect(Collectors.joining(", ")));
            }
            List<UserView> targetUsers = new ArrayList<>(userService.findAllUsersWithAdminRole());
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
                            targetUsers.add(userView);
                        });
            }
            mailAttributes.setAddresses(targetUsers);
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
            mailContent.setSubject((String) mailAttributes.getOtherAttributes().getOrDefault(MailTemplateElements.TITLE, "NMAAS: Broadcast message")); //set subject from other params
        }
        if (mailAttributes.getMailType().equals(MailType.CONTACT_FORM)) {
            Optional<String> contactFormKey = Optional.ofNullable((String) mailAttributes.getOtherAttributes().get("subType"));
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
        boolean showAdditional = mailAttributes.getMailType() == MailType.NEW_ACTIVE_APP && mailAttributes.getOtherAttributes().get("message") != null;
        Map<String, Object> map = new HashMap<>(mailTemplate.getGlobalInformation());
        map.put(MailTemplateElements.PORTAL_LINK, this.portalAddress == null ? "" : this.portalAddress);
        map.put(MailTemplateElements.HEADER, getHeader(langContent.getTemplate().get(MailTemplateElements.HEADER), user));
        map.put(MailTemplateElements.CONTENT, getContent(langContent.getTemplate().get(MailTemplateElements.CONTENT), mailAttributes.getOtherAttributes()));
        map.put(MailTemplateElements.ADDITIONAL, showAdditional ? getContent(langContent.getTemplate().get(MailTemplateElements.ADDITIONAL), mailAttributes.getOtherAttributes()) : "");
        map.put(MailTemplateElements.SENDER, langContent.getTemplate().get(MailTemplateElements.SENDER));
        map.put(MailTemplateElements.NOREPLY, langContent.getTemplate().get(MailTemplateElements.NOREPLY));
        map.put(MailTemplateElements.SENDER_POLICY, langContent.getTemplate().get(MailTemplateElements.SENDER_POLICY));
        map.put(MailTemplateElements.TITLE, langContent.getSubject());
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
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