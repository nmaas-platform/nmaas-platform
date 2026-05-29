package net.geant.nmaas.kubernetes.shell;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Slf4j
public class PodShellConnector extends KubernetesConnector {

    private static final int DEFAULT_COLUMNS = 120;
    private static final int DEFAULT_ROWS = 40;

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
                .exec(
                        "env",
                        "TERM=xterm",
                        "COLUMNS=" + DEFAULT_COLUMNS,
                        "LINES=" + DEFAULT_ROWS,
                        "bash",
                        "-lc",
                        "stty cols " + DEFAULT_COLUMNS + " rows " + DEFAULT_ROWS + "; exec bash -i"
                );
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
