package net.geant.nmaas.notifications;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.notifications.templates.TemplateService;
import net.geant.nmaas.notifications.templates.api.LanguageMailContentView;
import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import net.geant.nmaas.portal.api.domain.UserView;
import net.geant.nmaas.portal.persistent.entity.User;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class NotificationManagerTest {

    private NotificationManager notificationManager;

    private NotificationService notificationService = mock(NotificationService.class);

    private TemplateService templateService = mock(TemplateService.class);

    private UserService userService = mock(UserService.class);

    private DomainService domainService = mock(DomainService.class);

    private ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    public void setup() {
        this.notificationManager = new NotificationManager(templateService, notificationService, userService, domainService, modelMapper);
    }

    @Test
    public void notificationServiceShouldSentEmail() {
        JavaMailSender jms = mock(JavaMailSender.class);
        this.notificationService = new NotificationService(jms);

        this.notificationService.sendMail("mail", "subject", "content");

        verify(jms, times(1)).send(any(MimeMessagePreparator.class));
    }

    @Test
    public void shouldSendBroadcastEmail() throws IOException {

        when(userService.findAllUsersWithAdminRole()).thenReturn(this.getDefaultUserList().stream().map(user -> modelMapper.map(user, UserView.class)).collect(Collectors.toList()));
        when(userService.findUsersWithRoleSystemAdminAndOperator()).thenReturn(this.getDefaultUserList().stream().map(user -> modelMapper.map(user, UserView.class)).collect(Collectors.toList()));
        when(userService.findAll()).thenReturn(this.getDefaultUserList());

        MailTemplateView mt = this.getDefaultMailTemplateView();
        when(templateService.getMailTemplate(any())).thenReturn(mt);

        MailAttributes ma = new MailAttributes();
        ma.setMailType(MailType.BROADCAST);
        ma.setOtherAttributes(new HashMap<String, String>() {{
            put("text", "some text");

            put(MailTemplateElements.TITLE, "Some Title");
            put("username", "MyUser");
        }});

        Template template = mock(Template.class);
        when(templateService.getHTMLTemplate()).thenReturn(template);

        try {
            notificationManager.prepareAndSendMail(ma);
        } catch (IOException ioe) {
            fail("IO exception caught " + ioe.getMessage());
        } catch (TemplateException te) {
            fail("Template exception caught " + te.getMessage());
        }

        verify(notificationService, times(1)).sendMail(any(String.class), eq("Some Title"), any(String.class));

    }

    private MailTemplateView getDefaultMailTemplateView() {
        LanguageMailContentView lmcv = new LanguageMailContentView();
        lmcv.setLanguage("en");
        lmcv.setSubject("default");
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
        mt.setTemplates(new ArrayList<LanguageMailContentView>() {{
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

        return new ArrayList<User>() {{
            add(user0);
            add(user1);
        }};
    }

    // TODO unit tests
}
