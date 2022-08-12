package net.geant.nmaas.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
class NotificationService {

    private final JavaMailSender mailSender;

    void sendMail(String mail, String subject, String template, String fromAddress) {
        executeSendMail(mail, subject, template, fromAddress);
    }

    void sendMail(String mail, String subject, String template) {
        executeSendMail(mail, subject, template, null);
    }

    private void executeSendMail(String mail, String subject, String template, String fromAddress) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setTo(mail);
            messageHelper.setSubject(subject);
            messageHelper.setText(template, true);
            if (Objects.nonNull(fromAddress)) {
                messageHelper.setFrom(fromAddress);
            }
        };
        mailSender.send(messagePreparator);
    }

}
