package net.geant.nmaas.notifications;

import freemarker.template.Template;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.notifications.templates.TemplateService;
import net.geant.nmaas.notifications.templates.api.LanguageMailContentView;
import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import net.geant.nmaas.notifications.types.persistence.entity.FormType;
import net.geant.nmaas.notifications.types.service.FormTypeService;
import net.geant.nmaas.portal.api.configuration.ConfigurationView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.api.exception.ProcessingException;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificationManagerTest {

    private NotificationService notificationService = mock(NotificationService.class);
    private final TemplateService templateService = mock(TemplateService.class);
    private final UserService userService = mock(UserService.class);
    private final DomainService domainService = mock(DomainService.class);
    private final ConfigurationManager configurationManager = mock(ConfigurationManager.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final FormTypeService formTypeService = mock(FormTypeService.class);
    private NotificationEventListener notificationEventListener;

    private NotificationManager notificationManager;

    @BeforeEach
    void setup() throws IOException {
        this.notificationManager = new NotificationManager(templateService, notificationService, userService, domainService, configurationManager, modelMapper, formTypeService);

        when(userService.findAllUsersWithAdminRole()).thenReturn(
                this.getAdminUserList().stream()
                        .map(user -> modelMapper.map(user, UserView.class))
                        .collect(Collectors.toList())
        );
        when(userService.findUsersWithRoleSystemAdminAndOperator()).thenReturn(
                this.getAdminUserList().stream()
                        .map(user -> modelMapper.map(user, UserView.class))
                        .collect(Collectors.toList())
        );
        when(userService.findAll()).thenReturn(this.getDefaultUserList());
        when(domainService.findUsersWithDomainAdminRole("domainName")).thenReturn(
                this.getAdminUserList().stream()
                        .map(u -> this.modelMapper.map(u, UserView.class))
                        .collect(Collectors.toList())
        );
        when(userService.findByUsername("ordinary")).thenReturn(Optional.of(this.getDefaultUserList().get(1)));

        MailTemplateView mt = this.getDefaultMailTemplateView();
        when(templateService.getMailTemplate(any())).thenReturn(mt);

        Template template = mock(Template.class);
        when(templateService.getHTMLTemplate()).thenReturn(template);
    }

    @Test
    void notificationServiceShouldSentEmail() {
        JavaMailSender jms = mock(JavaMailSender.class);
        this.notificationService = new NotificationService(jms);

        this.notificationService.sendMail("mail", "subject", "content");

        verify(jms).send(any(MimeMessagePreparator.class));
    }

    @Test
    void notificationTaskShouldSendEmail() {
        notificationManager = mock(NotificationManager.class);
        notificationEventListener = new NotificationEventListener(notificationManager);
        MailAttributes ma = new MailAttributes();

        NotificationEvent event = new NotificationEvent(this, ma);
        notificationEventListener.trigger(event);

        verify(notificationManager).prepareAndSendMail(ma);
    }

    @Test
    void notificationTaskShouldNotSendMail() {
        notificationManager = mock(NotificationManager.class);
        notificationEventListener = new NotificationEventListener(notificationManager);

        NotificationEvent event = new NotificationEvent(this, null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> notificationEventListener.trigger(event));

        assertEquals("Mail attributes cannot be null", ex.getMessage());
    }

    @Test
    void shouldSendBroadcastEmail() {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.BROADCAST);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "some text");
            put(MailTemplateElements.TITLE, "Some Title");
            put("username", "MyUser");
        }});

        notificationManager.prepareAndSendMail(ma);

        verify(notificationService, times(2))
                .sendMail(any(String.class), eq("Some Title"), any(String.class));
    }

    @Test
    void shouldSendHealthCheckEmail() {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.EXTERNAL_SERVICE_HEALTH_CHECK);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "text");
            put("username", "MyUser");
        }});

        notificationManager.prepareAndSendMail(ma);

        verify(notificationService).sendMail(any(String.class), eq("Default"), any(String.class));
    }

    @Test
    void shouldSendHealthCheckEmailWithFromAddress() {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.EXTERNAL_SERVICE_HEALTH_CHECK);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "text");
            put("username", "MyUser");
        }});
        notificationManager.setFromAddress("noreply@from.address");

        notificationManager.prepareAndSendMail(ma);

        verify(notificationService).sendMail(any(String.class), eq("Default"), any(String.class), any(String.class));
    }

    @Test
    void shouldSendAppDeployedEmailToAdminWhenOwnerIsAdmin() {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.APP_DEPLOYED);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "text");
            put("username", "MyUser");
            put("owner", "admin");
            put("domainName", "domainName");
        }});

        notificationManager.prepareAndSendMail(ma);

        verify(notificationService).sendMail(any(String.class), eq("Default"), any(String.class));
    }

    @Test
    void shouldSendAppUpgradedEmailToAdminWhenOwnerIsAdmin() {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.APP_UPGRADED);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "text");
            put("username", "MyUser");
            put("owner", "admin");
            put("domainName", "domainName");
        }});

        notificationManager.prepareAndSendMail(ma);

        verify(notificationService).sendMail(any(String.class), eq("Default"), any(String.class));
    }

    @Test
    void shouldSendAppDeployedEmailToAdminAndOrdinaryWhenOwnerIsOrdinary() {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.APP_DEPLOYED);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "text");
            put("username", "MyUser");
            put("owner", "ordinary");
            put("domainName", "domainName");
        }});

        notificationManager.prepareAndSendMail(ma);

        verify(notificationService, times(2)).sendMail(any(String.class), eq("Default"), any(String.class));
    }

    @Test
    void shouldThrowExceptionWhenCannotFindTemplateWithMatchingLanguage() {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.REGISTRATION);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "text");
            put("username", "MyUser");
        }});

        List<User> adminUsers = this.getAdminUserList();
        adminUsers.get(0).setSelectedLanguage("fr");

        when(userService.findAllUsersWithAdminRole()).thenReturn(adminUsers.stream().map(u -> this.modelMapper.map(u, UserView.class)).collect(Collectors.toList()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> notificationManager.prepareAndSendMail(ma));
        assertEquals("Mail template in language fr cannot be found", ex.getMessage());
    }

    @Test
    void shouldSendContactForm() {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.CONTACT_FORM);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "text");
            put("subType", "CONTACT");
        }});

        List<User> adminUsers = this.getAdminUserList();
        when(userService.findAllUsersWithAdminRole()).thenReturn(adminUsers.stream().map(u -> this.modelMapper.map(u, UserView.class)).collect(Collectors.toList()));

        List<String> emails = new ArrayList<>();
        emails.add("email@man.poznan.pl");
        FormType ft = new FormType("CONTACT", "", "", emails, "Subject");
        when(formTypeService.findOne(anyString())).thenReturn(Optional.of(ft));

        notificationManager.prepareAndSendMail(ma);

        verify(notificationService, times(adminUsers.size() + emails.size()))
                .sendMail(any(String.class), eq("Subject"), any(String.class));
    }

    @Test
    void shouldSendAppDeploymentFailedEmail() {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.APP_DEPLOYMENT_FAILED);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "text");
            put("username", "MyUser");
            put("owner", "ordinary");
            put("domainName", "domainName");
        }});

        String external = "external@email.com";

        List<User> adminUsers = this.getAdminUserList();
        List<String> emails = Lists.newArrayList(
                adminUsers.get(0).getEmail(),
                external
        );

        when(userService.findByEmail(adminUsers.get(0).getEmail())).thenReturn(adminUsers.get(0));
        when(userService.findByEmail(external)).thenThrow(new IllegalArgumentException("test message"));

        when(configurationManager.getConfiguration()).thenReturn(new ConfigurationView(true, true, "en", true, true, emails));

        notificationManager.prepareAndSendMail(ma);

        verify(notificationService, times(emails.size())).sendMail(any(String.class), eq("Default"), any(String.class));
    }

    @Test
    void shouldThrowExceptionWhenTemplateServiceThrowsException() throws IOException {
        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.APP_DEPLOYED);
        ma.setOtherAttributes(new HashMap<>() {{
            put("text", "text");
            put("username", "MyUser");
            put("owner", "ordinary");
            put("domainName", "domainName");
        }});

        when(templateService.getHTMLTemplate()).thenThrow(new IOException());

        assertThrows(ProcessingException.class, () -> notificationManager.prepareAndSendMail(ma));
    }

    private MailTemplateView getDefaultMailTemplateView() {
        LanguageMailContentView lmcv = new LanguageMailContentView();
        lmcv.setLanguage("en");
        lmcv.setSubject("Default");
        lmcv.setTemplate(new HashMap<String, String>() {{
            put(MailTemplateElements.TITLE, "Default");
            put(MailTemplateElements.CONTENT, "${text}");
            put(MailTemplateElements.HEADER, "Dear ${username}");
            put(MailTemplateElements.NOREPLY, "");
            put(MailTemplateElements.PORTAL_LINK, "");
            put(MailTemplateElements.SENDER, "");
            put(MailTemplateElements.SENDER_POLICY, "");
        }});

        MailTemplateView mt = new MailTemplateView();
        mt.setTemplates(new ArrayList<>() {{
            add(lmcv);
        }});
        mt.setGlobalInformation(new HashMap<String, String>() {{
            put("LOGO_LINK", "https://www.geant.org/Style%20Library/Geant/Images/logo.png");
            put("LOGO_ALT", "Geant logo");
            put("PORTAL_LOGO_ALT", "NMaaS logo");
            put("SENDER_INFO", "&#9400; GÉANT Association Hoekenrode 3 1102 BR - Amsterdam – Zuidoost- The Netherlands");
        }});

        return mt;
    }

    private List<User> getDefaultUserList() {
        User user0 = new User("admin", true);
        user0.setEmail("admin@admin.eu");
        user0.setSelectedLanguage("en");
        User user1 = new User("ordinary", true);
        user1.setEmail("ordinary@email.com");
        user1.setSelectedLanguage("en");

        return new ArrayList<>() {{
            add(user0);
            add(user1);
        }};
    }

    private List<User> getAdminUserList() {
        User user0 = new User("admin", true);
        user0.setEmail("admin@admin.eu");
        user0.setFirstname("");
        user0.setLastname("");
        user0.setSelectedLanguage("en");

        return new ArrayList<>() {{
            add(user0);
        }};
    }
}
