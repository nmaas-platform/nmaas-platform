package net.geant.nmaas.dcn.deployment;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig;
import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig.Action;
import net.geant.nmaas.dcn.deployment.entities.AnsiblePlaybookVpnConfig.Type;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukasz Lopatowski <llopat@man.poznan.pl>
 */
@Component
@Profile("dcn_ansible")
public class AnsiblePlaybookContainerBuilder {

    private static String ansibleDockerImageName;
    private static String ansiblePlaybookNameForClientRouterConfigAdd;
    private static String ansiblePlaybookNameForClientRouterConfigRem;
    private static String ansiblePlaybookNameForCloudRouterConfigAdd;
    private static String ansiblePlaybookNameForCloudRouterConfigRem;

    @Value("${ansible.docker.image}")
    public void setAnsibleDockerImage(String ansibleDockerImageNameProperty) {
        ansibleDockerImageName = ansibleDockerImageNameProperty;
    }

    @Value("${ansible.playbook.client.router.config.add}")
    public void setAnsiblePlaybookNameForClientRouterConfigAdd(String ansiblePlaybookNameForClientRouterConfigAddProperty) {
        ansiblePlaybookNameForClientRouterConfigAdd = ansiblePlaybookNameForClientRouterConfigAddProperty;
        ansibleVolumeOnContainerPlaybookFileClientRouterConfigAdd = "/ansible/" + ansiblePlaybookNameForClientRouterConfigAdd;
    }

    @Value("${ansible.playbook.client.router.config.rem}")
    public void setAnsiblePlaybookNameForClientRouterConfigRem(String ansiblePlaybookNameForClientRouterConfigRemProperty) {
        ansiblePlaybookNameForClientRouterConfigRem = ansiblePlaybookNameForClientRouterConfigRemProperty;
        ansibleVolumeOnContainerPlaybookFileClientRouterConfigRem = "/ansible/" + ansiblePlaybookNameForClientRouterConfigRem;
    }

    @Value("${ansible.playbook.cloud.router.config.add}")
    public void setAnsiblePlaybookNameForCloudRouterConfigAdd(String ansiblePlaybookNameForCloudRouterConfigAddProperty) {
        ansiblePlaybookNameForCloudRouterConfigAdd = ansiblePlaybookNameForCloudRouterConfigAddProperty;
        ansibleVolumeOnContainerPlaybookFileCloudRouterConfigAdd = "/ansible/" + ansiblePlaybookNameForCloudRouterConfigAdd;
    }

    @Value("${ansible.playbook.cloud.router.config.rem}")
    public void setAnsiblePlaybookNameForCloudRouterConfigRem(String ansiblePlaybookNameForCloudRouterConfigRemProperty) {
        ansiblePlaybookNameForCloudRouterConfigRem = ansiblePlaybookNameForCloudRouterConfigRemProperty;
        ansibleVolumeOnContainerPlaybookFileCloudRouterConfigRem = "/ansible/" + ansiblePlaybookNameForCloudRouterConfigRem;
    }

    private static final String ANSIBLE_DIR_ON_DOCKER_HOST = "/home/docker/ansible-docker/";
    private static final String ANSIBLE_VOLUME_1_FROM = ANSIBLE_DIR_ON_DOCKER_HOST + "ansible.cfg";
    private static final String ANSIBLE_VOLUME_1_TO = "/etc/ansible/ansible.cfg";
    private static String ansibleVolumeOnContainerPlaybookFileClientRouterConfigAdd;
    private static String ansibleVolumeOnContainerPlaybookFileClientRouterConfigRem;
    private static String ansibleVolumeOnContainerPlaybookFileCloudRouterConfigAdd;
    private static String ansibleVolumeOnContainerPlaybookFileCloudRouterConfigRem;
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
        containerBuilder.image(ansibleDockerImageName);
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
                        .from(pathToFileInHostAnsibleConfigDirectory(ansiblePlaybookNameForCloudRouterConfigAdd))
                        .to(ansibleVolumeOnContainerPlaybookFileCloudRouterConfigAdd)
                        .build().toString());
            else
                volumeBinds.add(HostConfig.Bind
                        .from(pathToFileInHostAnsibleConfigDirectory(ansiblePlaybookNameForClientRouterConfigAdd))
                        .to(ansibleVolumeOnContainerPlaybookFileClientRouterConfigAdd)
                        .build().toString());
        } else {
            if (commandForCloudSideRouter(type))
                volumeBinds.add(HostConfig.Bind
                        .from(pathToFileInHostAnsibleConfigDirectory(ansiblePlaybookNameForCloudRouterConfigRem))
                        .to(ansibleVolumeOnContainerPlaybookFileCloudRouterConfigRem)
                        .build().toString());
            else
                volumeBinds.add(HostConfig.Bind
                        .from(pathToFileInHostAnsibleConfigDirectory(ansiblePlaybookNameForClientRouterConfigRem))
                        .to(ansibleVolumeOnContainerPlaybookFileClientRouterConfigRem)
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