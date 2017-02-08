package net.geant.nmaas.dcndeployment;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static net.geant.nmaas.dcndeployment.AnsiblePlaybookCommandBuilder.command;

@Service
public class AnsibleContainerConfigBuilder {

    private static final String ANSIBLE_IMAGE_NAME = "a4aea6924d2d";
    private static final String ANSIBLE_DIR_ON_DOCKER_HOST = "/home/docker/ansible-docker/";
    private static final String ANSIBLE_VOLUME_1_FROM = ANSIBLE_DIR_ON_DOCKER_HOST + "ansible.cfg";
    private static final String ANSIBLE_VOLUME_1_TO = "/etc/ansible/ansible.cfg";
    private static final String ANSIBLE_VOLUME_2_FROM = ANSIBLE_DIR_ON_DOCKER_HOST + "pb-nmaas-vpn-client-test-config.yml";
    private static final String ANSIBLE_VOLUME_2_TO = "/ansible/pb-nmaas-vpn-client-test-config.yml";
    private static final String ANSIBLE_VOLUME_3_FROM = ANSIBLE_DIR_ON_DOCKER_HOST + "working-dir/config-set";
    private static final String ANSIBLE_VOLUME_3_TO = "/ansible-config-set";
    private static final String ANSIBLE_VOLUME_4_FROM = ANSIBLE_DIR_ON_DOCKER_HOST;
    private static final String ANSIBLE_VOLUME_4_TO = "/ansible-playbook-dir";
    private static final String ANSIBLE_VOLUME_5_FROM = "/home/docker/.ssh/id_rsa";
    private static final String ANSIBLE_VOLUME_5_TO = "/root/.ssh/id_rsa";

    public static ContainerConfig build(VpnConfig vpn, String dcnId) {
        final ContainerConfig.Builder containerBuilder = ContainerConfig.builder();
        containerBuilder.image(ANSIBLE_IMAGE_NAME);
        containerBuilder.cmd(command(vpn, dcnId));
        final HostConfig.Builder hostBuilder = HostConfig.builder();
        hostBuilder.appendBinds(volumeBindings(vpn));
        containerBuilder.hostConfig(hostBuilder.build());
        return containerBuilder.build();
    }

    private static List<String> volumeBindings(VpnConfig vpn) {
        final List<String> volumeBinds = new ArrayList<>();
        volumeBinds.add(HostConfig.Bind.from(ANSIBLE_VOLUME_1_FROM).to(ANSIBLE_VOLUME_1_TO).build().toString());
        volumeBinds.add(HostConfig.Bind.from(ANSIBLE_VOLUME_2_FROM).to(ANSIBLE_VOLUME_2_TO).build().toString());
        volumeBinds.add(HostConfig.Bind.from(ANSIBLE_VOLUME_3_FROM).to(ANSIBLE_VOLUME_3_TO).build().toString());
        volumeBinds.add(HostConfig.Bind.from(ANSIBLE_VOLUME_4_FROM).to(ANSIBLE_VOLUME_4_TO).build().toString());
        volumeBinds.add(HostConfig.Bind.from(ANSIBLE_VOLUME_5_FROM).to(ANSIBLE_VOLUME_5_TO).build().toString());
        return volumeBinds;
    }

}
