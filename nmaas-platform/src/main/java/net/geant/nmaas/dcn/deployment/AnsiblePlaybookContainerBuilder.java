package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static net.geant.nmaas.dcn.deployment.AnsiblePlaybookVpnConfig.*;

@Component
public class AnsiblePlaybookContainerBuilder {

    private static final String ANSIBLE_IMAGE_NAME = "nmaas/ansible:2.3.0";
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER_CONFIG = "pb-nmaas-vpn-asbr-config.yml";
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER_CONFIG = "pb-nmaas-vpn-iaas-config.yml";
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER_CONFIG_REMOVAL = "pb-nmaas-vpn-asbr-delete.yml";
    private static final String ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER_CONFIG_REMOVAL = "pb-nmaas-vpn-iaas-delete.yml";
    private static final String ANSIBLE_DIR_ON_DOCKER_HOST = "/home/docker/ansible-docker/";
    private static final String ANSIBLE_VOLUME_1_FROM = ANSIBLE_DIR_ON_DOCKER_HOST + "ansible.cfg";
    private static final String ANSIBLE_VOLUME_1_TO = "/etc/ansible/ansible.cfg";
    private static final String ANSIBLE_VOLUME_PLAYBOOK_FILE_CLIENT_SIDE_ROUTER_TO = "/ansible/" + ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER_CONFIG;
    private static final String ANSIBLE_VOLUME_PLAYBOOK_FILE_CLOUD_SIDE_ROUTER_TO = "/ansible/" + ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER_CONFIG;
    private static final String ANSIBLE_VOLUME_3_FROM = ANSIBLE_DIR_ON_DOCKER_HOST + "working-dir/config-set";
    private static final String ANSIBLE_VOLUME_3_TO = "/ansible-config-set";
    private static final String ANSIBLE_VOLUME_4_FROM = ANSIBLE_DIR_ON_DOCKER_HOST;
    private static final String ANSIBLE_VOLUME_4_TO = "/ansible-playbook-dir";
    private static final String ANSIBLE_VOLUME_5_FROM = "/home/docker/.ssh/id_rsa";
    private static final String ANSIBLE_VOLUME_5_TO = "/root/.ssh/id_rsa";

    public static ContainerConfig buildContainerForClientSideRouterConfig(AnsiblePlaybookVpnConfig vpn, String dcnId) {
        return buildConfigContainer(Action.ADD, Type.CLIENT_SIDE, vpn, dcnId);
    }

    public static ContainerConfig buildContainerForCloudSideRouterConfig(AnsiblePlaybookVpnConfig vpn, String dcnId) {
        return buildConfigContainer(Action.ADD, Type.CLOUD_SIDE, vpn, dcnId);
    }

    public static ContainerConfig buildContainerForClientSideRouterConfigRemoval(AnsiblePlaybookVpnConfig vpn, String dcnId) {
        return buildConfigContainer(Action.REMOVE, Type.CLIENT_SIDE, vpn, dcnId);
    }

    public static ContainerConfig buildContainerForCloudSideRouterConfigRemoval(AnsiblePlaybookVpnConfig vpn, String dcnId) {
        return buildConfigContainer(Action.REMOVE, Type.CLOUD_SIDE, vpn, dcnId);
    }

    private static ContainerConfig buildConfigContainer(Action action, Type type, AnsiblePlaybookVpnConfig vpn, String dcnId) {
        final ContainerConfig.Builder containerBuilder = ContainerConfig.builder();
        containerBuilder.image(ANSIBLE_IMAGE_NAME);
        containerBuilder.cmd(AnsiblePlaybookCommandBuilder.command(action, type, vpn, dcnId));
        final HostConfig.Builder hostBuilder = HostConfig.builder();
        hostBuilder.appendBinds(volumeBindings(action, type, vpn));
        containerBuilder.hostConfig(hostBuilder.build());
        return containerBuilder.build();
    }

    private static List<String> volumeBindings(Action action, Type type, AnsiblePlaybookVpnConfig vpn) {
        final List<String> volumeBinds = new ArrayList<>();
        volumeBinds.add(HostConfig.Bind.from(ANSIBLE_VOLUME_1_FROM).to(ANSIBLE_VOLUME_1_TO).build().toString());

        if (commandForConfigAddition(action)) {
            if (commandForCloudSideRouter(type))
                volumeBinds.add(HostConfig.Bind
                        .from(pathToFileInHostAnsibleConfigDirectory(ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER_CONFIG))
                        .to(ANSIBLE_VOLUME_PLAYBOOK_FILE_CLOUD_SIDE_ROUTER_TO)
                        .build().toString());
            else
                volumeBinds.add(HostConfig.Bind
                        .from(pathToFileInHostAnsibleConfigDirectory(ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER_CONFIG))
                        .to(ANSIBLE_VOLUME_PLAYBOOK_FILE_CLIENT_SIDE_ROUTER_TO)
                        .build().toString());
        } else {
            if (commandForCloudSideRouter(type))
                volumeBinds.add(HostConfig.Bind
                        .from(pathToFileInHostAnsibleConfigDirectory(ANSIBLE_PLAYBOOK_NAME_FOR_CLOUD_SIDE_ROUTER_CONFIG_REMOVAL))
                        .to(ANSIBLE_VOLUME_PLAYBOOK_FILE_CLOUD_SIDE_ROUTER_TO)
                        .build().toString());
            else
                volumeBinds.add(HostConfig.Bind
                        .from(pathToFileInHostAnsibleConfigDirectory(ANSIBLE_PLAYBOOK_NAME_FOR_CLIENT_SIDE_ROUTER_CONFIG_REMOVAL))
                        .to(ANSIBLE_VOLUME_PLAYBOOK_FILE_CLIENT_SIDE_ROUTER_TO)
                        .build().toString());
        }
        volumeBinds.add(HostConfig.Bind.from(ANSIBLE_VOLUME_3_FROM).to(ANSIBLE_VOLUME_3_TO).build().toString());
        volumeBinds.add(HostConfig.Bind.from(ANSIBLE_VOLUME_4_FROM).to(ANSIBLE_VOLUME_4_TO).build().toString());
        volumeBinds.add(HostConfig.Bind.from(ANSIBLE_VOLUME_5_FROM).to(ANSIBLE_VOLUME_5_TO).build().toString());
        return volumeBinds;
    }

    private static boolean commandForConfigAddition(Action action) {
        return action.equals(Action.ADD);
    }

    private static boolean commandForCloudSideRouter(Type type) {
        return type.equals(Type.CLOUD_SIDE);
    }

    private static String pathToFileInHostAnsibleConfigDirectory(String fileName) {
        return ANSIBLE_DIR_ON_DOCKER_HOST + fileName;
    }

}