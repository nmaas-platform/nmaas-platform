package net.geant.nmaas.kubernetes.shell;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.kubernetes.KubernetesConnector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Log4j2
public class PodShellConnector extends KubernetesConnector {

    protected ExecWatch watch;

    public PodShellConnector(KubernetesClient client, String namespace, String podName) {
        this.namespace = namespace;
        this.podName = podName;
        this.client = client;
        this.initWatch();
    }

    public PodShellConnector(KubernetesClient client, String namespace, String podName, ExecWatch watch) {
        this.namespace = namespace;
        this.podName = podName;
        this.client = client;
        this.watch = watch;
    }

    private void initWatch() {
        log.debug("Initializing exec watch");
        watch = client.pods()
                .inNamespace(namespace)
                .withName(podName)
                .redirectingInput()
                .redirectingOutput()
                .redirectingError()
                .withTTY()
                .usingListener(new SimpleListener())
                .exec();
    }

    @Override
    public void executeCommand(String command) {
        try {
            watch.getInput().write((command + "\n").getBytes());
            watch.getInput().flush();
        } catch (IOException e) {
            log.error("Command execution failed ({})", command, e);
        }
    }

    @Override
    public InputStream getInputStream() {
        assert Objects.nonNull(watch);
        return watch.getOutput();
    }

    @Override
    public InputStream getErrorStream() {
        assert Objects.nonNull(watch);
        return watch.getError();
    }

    @Override
    public void close() {
        watch.close();
    }

}
