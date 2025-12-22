package net.geant.nmaas.kubernetes.remote;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.domain.UserView;
import net.geant.nmaas.portal.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoteClusterMailer {

    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final ModelMapper modelMapper;

    void sendMail(KCluster kCluster, MailType mailType) {
        UserView recipient;
        if (userService.existsByEmail(kCluster.getContactEmail())) {
            recipient = modelMapper.map(userService.findByEmail(kCluster.getContactEmail()), UserView.class);
        } else {
            recipient = UserView.builder().email(kCluster.getContactEmail()).username(kCluster.getContactEmail()).selectedLanguage("EN").build();
        }

        Map<String, Object> attr = new HashMap<>();
        attr.put("clusterId", kCluster.getId());
        attr.put("clusterCodename", kCluster.getCodename());
        attr.put("clusterName", kCluster.getName());
        MailAttributes mailAttributes = MailAttributes.builder()
                .mailType(mailType)
                .otherAttributes(attr)
                .addresses(Collections.singletonList(recipient))
                .build();

        this.eventPublisher.publishEvent(new NotificationEvent(this, mailAttributes));
    }

}
