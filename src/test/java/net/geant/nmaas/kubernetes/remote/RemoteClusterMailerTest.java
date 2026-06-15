package net.geant.nmaas.kubernetes.remote;

import net.geant.nmaas.kubernetes.remote.entities.KCluster;
import net.geant.nmaas.notifications.MailAttributes;
import net.geant.nmaas.notifications.NotificationEvent;
import net.geant.nmaas.notifications.templates.MailType;
import net.geant.nmaas.portal.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoteClusterMailerTest {

    private final UserService userService = mock(UserService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final RemoteClusterMailer mailer = new RemoteClusterMailer(userService, eventPublisher, new ModelMapper());

    @Test
    void shouldPublishSupportEmailAttributesWithClusterName() {
        KCluster cluster = KCluster.builder()
                .id(1L)
                .name("Readable Cluster Name")
                .codename("cluster-codename")
                .contactEmail("support@example.test")
                .build();
        when(userService.existsByEmail(cluster.getContactEmail())).thenReturn(false);

        mailer.sendMail(cluster, MailType.REMOTE_CLUSTER_WELCOME_SUPPORT);

        ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        MailAttributes mailAttributes = eventCaptor.getValue().getMailAttributes();
        assertThat(mailAttributes.getMailType()).isEqualTo(MailType.REMOTE_CLUSTER_WELCOME_SUPPORT);
        assertThat(mailAttributes.getOtherAttributes())
                .containsEntry("clusterName", "Readable Cluster Name")
                .containsEntry("clusterCodename", "cluster-codename");
        assertThat(mailAttributes.getAddresses()).singleElement()
                .satisfies(recipient -> assertThat(recipient.getEmail()).isEqualTo("support@example.test"));
    }
}
