package net.geant.nmaas.notifications;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
class NotificationService {

    private JavaMailSender mailSender;

    @Autowired
    NotificationService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    void sendMail(String mail, String subject, String template){
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setTo(mail);
            messageHelper.setSubject(subject);
            messageHelper.setText(template, true);
        };
        mailSender.send(messagePreparator);
    }

}
