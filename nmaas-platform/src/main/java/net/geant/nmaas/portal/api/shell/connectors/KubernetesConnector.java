package net.geant.nmaas.portal.api.shell.connectors;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.extern.log4j.Log4j2;
import okhttp3.Response;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
            log.info("shell barfed");
        }

        @Override
        public void onClose(int code, String reason) {
            log.info("The shell will now close.");
        }
    }

    private final String podName;
    private final String namespace;
    private final String master;

    protected Config config;
    protected KubernetesClient client;
    protected ExecWatch watch;

    public KubernetesConnector() {
        podName = "default";
        namespace = "default";
        master = "https://localhost:8443/";

        this.init();
    }

    public KubernetesConnector(String podName, String namespace, String master) {
        this.podName = podName;
        this.namespace = namespace;
        this.master = master;

        this.init();
    }

    protected void init() {
        config = new ConfigBuilder().withMasterUrl(master).build();
        client = new DefaultKubernetesClient(config);
        watch = client.pods()
                .inNamespace(namespace)
                .withName(podName)
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
        throw new NotImplementedException();
    }

}
