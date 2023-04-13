package net.geant.nmaas.kubernetes.client;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.extern.log4j.Log4j2;
import net.geant.nmaas.kubernetes.shell.connectors.AsyncConnector;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;

@Log4j2
public class KubernetesConnector implements AsyncConnector {

    protected static class SimpleListener implements ExecListener {
        @Override
        public void onOpen(Response response) {
            log.info("Shell opened");
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            log.info("Shell barfed");
        }

        @Override
        public void onClose(int code, String reason) {
            log.info("Shell will now close.");
        }
    }

    protected String podName;
    protected String namespace;

    protected transient Config config;
    protected transient KubernetesClient client;
    protected transient ExecWatch watch;

    protected KubernetesConnector(){}

    public KubernetesConnector(KubernetesClient client, String namespace, String podName) {
        this.namespace = namespace;
        this.podName = podName;
        this.client = client;

        this.initWatch();
    }

    protected void initWatch() {
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

    public void executeCommand(String command) {
        try {
            watch.getInput().write((command + "\n").getBytes());
            watch.getInput().flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public InputStream getInputStream() {
        return watch.getOutput();
    }

    public InputStream getErrorStream() {
        return watch.getError();
    }

    public void close() {
        watch.close();
    }

    public String executeSingleCommand(String command) {
        return "NOT IMPLEMENTED";
    }

}
