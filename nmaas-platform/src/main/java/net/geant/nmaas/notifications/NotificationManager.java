package net.geant.nmaas.notifications;

import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.notifications.templates.api.LanguageMailContentView;
import net.geant.nmaas.notifications.templates.api.MailTemplateView;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.notifications.templates.TemplateService;
import net.geant.nmaas.orchestration.api.model.AppDeploymentView;
import net.geant.nmaas.portal.api.domain.User;
import net.geant.nmaas.portal.service.ConfigurationManager;
import net.geant.nmaas.portal.service.DomainService;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class NotificationManager {

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

    void prepareAndSendMail(MailAttributes mailAttributes){
        MailTemplateView mailTemplate = templateService.getMailTemplate(mailAttributes.getMailType());
        LanguageMailContentView langTemplate = mailTemplate.getTemplates().stream()
                .filter(temp -> temp.getLanguage().equals(configurationManager.getConfiguration().getDefaultLanguage()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Mail template not found"));
        this.prepareMail(mailAttributes);
        for(User user : mailAttributes.getAddressees()){
            this.notificationService.sendMail(user.getEmail(), langTemplate.getSubject(), getFilledTemplate(langTemplate.getTemplate(), user, mailAttributes.getAppDeploymentView(), mailAttributes.getOtherAttribute()));
        }
        log.info("Mail " + mailAttributes.getMailType().name() + " was sent to " + getListOfMails(mailAttributes.getAddressees()));
    }

    private void prepareMail(MailAttributes mailAttributes){
        if(mailAttributes.getMailType().equals(MailType.EXTERNAL_SERVICE_HEALTH_CHECK)){
            mailAttributes.setAddressees(userService.findUsersWithRoleSystemAdminAndOperator());
        }
        if(mailAttributes.getMailType().equals(MailType.REGISTRATION)){
            mailAttributes.setAddressees(userService.findAllUsersWithAdminRole());
        }
        if(mailAttributes.getMailType().equals(MailType.APP_DEPLOYED)){
            mailAttributes.setAddressees(domainService.findUsersWithDomainAdminRole(mailAttributes.getAppDeploymentView().getDomain()));
            if(mailAttributes.getAddressees().stream().noneMatch(user -> user.getUsername().equals(mailAttributes.getAppDeploymentView().getOwner()))){
                userService.findByUsername(mailAttributes.getAppDeploymentView().getOwner())
                        .ifPresent(user -> mailAttributes.getAddressees().add(modelMapper.map(user, User.class)));
            }
        }
    }

    private String getFilledTemplate(String template, User user, AppDeploymentView appDeployment, String other){
        return template.replace("#username", user.getFirstname() == null ? user.getUsername() : user.getFirstname())
                .replace("#appName", appDeployment == null ? "" : appDeployment.getAppName())
                .replace("#appInstanceName", appDeployment  == null ? "" : appDeployment.getDeploymentName())
                .replace("#domainName", appDeployment  == null ? "" : appDeployment.getDomain())
                .replace("#serviceName", other == null ? "" : other)
                .replace("#newUser", other == null ? "" : other)
                .replace("#accessURL", other  == null ? "" : other);
    }

    private List<String> getListOfMails(List<User> users){
        return users.stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

}
