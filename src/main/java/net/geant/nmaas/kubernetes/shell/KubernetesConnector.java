package net.geant.nmaas.kubernetes.shell;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;

import java.io.InputStream;

@Slf4j
public abstract class KubernetesConnector implements AsyncConnector {

    public static class SimpleListener implements ExecListener {
        @Override
        public void onOpen() {
            log.info("Shell opened");
        }

        @Override
        public void onFailure(Throwable t, Response response) {
            log.info("Shell connection broke");
        }

        @Override
        public void onClose(int code, String reason) {
            log.info("Shell connection will now close");
        }
    }

    protected String podName;
    protected String namespace;

    protected transient Config config;
    protected transient KubernetesClient client;

    @Override
    public void executeCommand(String command) {
        throw new NotImplementedException();
    }

    @Override
    public InputStream getErrorStream() {
        throw new NotImplementedException();
    }

    @Override
    public void close() {
        throw new NotImplementedException();
    }

    @Override
    public String executeSingleCommand(String command) {
        throw new NotImplementedException();
    }

}
